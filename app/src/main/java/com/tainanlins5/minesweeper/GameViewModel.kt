package com.tainanlins5.minesweeper

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tainanlins5.minesweeper.data.AppSettings
import com.tainanlins5.minesweeper.data.AppearanceMode
import com.tainanlins5.minesweeper.data.CustomBoardPreset
import com.tainanlins5.minesweeper.data.GameResult
import com.tainanlins5.minesweeper.data.GameLayout
import com.tainanlins5.minesweeper.domain.Difficulty
import com.tainanlins5.minesweeper.domain.GameState
import com.tainanlins5.minesweeper.domain.GameStatus
import com.tainanlins5.minesweeper.domain.MinesweeperEngine
import com.tainanlins5.minesweeper.domain.ResultType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreen {
    GAME,
    RECORDS,
    SETTINGS,
}

enum class SoundEffect {
    REVEAL,
    FLAG,
    WIN,
    LOSE,
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as MinesweeperApplication
    private val gameRepository = app.gameRepository
    private val settingsRepository = app.settingsRepository

    private val _game = MutableStateFlow(MinesweeperEngine.newGame(Difficulty.BEGINNER))
    val game = _game.asStateFlow()
    private val _displayElapsedMillis = MutableStateFlow(0L)
    val displayElapsedMillis = _displayElapsedMillis.asStateFlow()
    val screen = MutableStateFlow(AppScreen.GAME)
    val isFlagMode = MutableStateFlow(false)
    val isLoaded = MutableStateFlow(false)
    val resultDialogVisible = MutableStateFlow(false)

    val settings = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppSettings(),
    )
    val results = gameRepository.observeResults().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _soundEffects = MutableSharedFlow<SoundEffect>(extraBufferCapacity = 4)
    val soundEffects = _soundEffects.asSharedFlow()
    private var runningSinceRealtime: Long? = null
    private var timerJob: Job? = null
    private var isAppResumed = false

    init {
        viewModelScope.launch {
            val (restored, defaultFlagMode) = withContext(Dispatchers.IO) {
                gameRepository.loadActiveGame() to settingsRepository.settings.first().flagModeDefault
            }
            if (restored != null) _game.value = restored
            publishDisplayElapsed(_game.value.elapsedMillis)
            isFlagMode.value = defaultFlagMode
            isLoaded.value = true
            if (isAppResumed && _game.value.status == GameStatus.ACTIVE && screen.value == AppScreen.GAME) startClock()
        }
    }

    fun showScreen(value: AppScreen) {
        if (screen.value == AppScreen.GAME && value != AppScreen.GAME) {
            resultDialogVisible.value = false
            pauseClock()
        }
        screen.value = value
        if (value == AppScreen.GAME) onAppResumed()
    }

    fun dismissResultDialog() {
        resultDialogVisible.value = false
    }

    fun reveal(index: Int) {
        val current = _game.value
        if (current.status == GameStatus.WON || current.status == GameStatus.LOST) return
        val now = SystemClock.elapsedRealtime()
        val before = withCurrentElapsed(current, now)
        val after = if (before.cells.getOrNull(index)?.isRevealed == true) {
            MinesweeperEngine.chord(before, index)
        } else {
            MinesweeperEngine.reveal(before, index)
        }
        if (after == before) return
        if (before.status == GameStatus.READY && after.status == GameStatus.ACTIVE) {
            runningSinceRealtime = now
            startTimer()
        } else if (current.status == GameStatus.ACTIVE) {
            runningSinceRealtime = if (after.status == GameStatus.ACTIVE) now else null
        }
        updateGame(after, SoundEffect.REVEAL)
    }

    fun toggleFlag(index: Int) {
        val current = _game.value
        val now = SystemClock.elapsedRealtime()
        val before = withCurrentElapsed(current, now)
        val after = MinesweeperEngine.toggleFlag(before, index)
        if (after == before) return
        if (current.status == GameStatus.ACTIVE) runningSinceRealtime = now
        updateGame(after, SoundEffect.FLAG)
    }

    fun toggleFlagMode() {
        isFlagMode.value = !isFlagMode.value
    }

    fun startGame(difficulty: Difficulty, width: Int = difficulty.width, height: Int = difficulty.height, mines: Int = difficulty.mines) {
        val current = withCurrentElapsed(_game.value, SystemClock.elapsedRealtime())
        stopClock()
        resultDialogVisible.value = false
        val newGame = MinesweeperEngine.newGame(difficulty, width, height, mines)
        _game.value = newGame
        publishDisplayElapsed(0L)
        screen.value = AppScreen.GAME
        viewModelScope.launch(Dispatchers.IO) {
            if (current.hasStarted && current.status == GameStatus.ACTIVE) {
                gameRepository.addResult(current, ResultType.ABANDONED)
            }
            gameRepository.saveActiveGame(newGame)
        }
    }

    fun restartGame() = startGame(
        _game.value.difficulty,
        _game.value.width,
        _game.value.height,
        _game.value.mineCount,
    )

    fun onAppPaused() {
        isAppResumed = false
        pauseClock()
    }

    private fun pauseClock() {
        val state = withCurrentElapsed(_game.value, SystemClock.elapsedRealtime())
        _game.value = state
        publishDisplayElapsed(state.elapsedMillis)
        stopClock()
        if (state.status == GameStatus.READY || state.status == GameStatus.ACTIVE) {
            viewModelScope.launch(Dispatchers.IO) { gameRepository.saveActiveGame(state) }
        }
    }

    fun onAppResumed() {
        isAppResumed = true
        if (isLoaded.value && screen.value == AppScreen.GAME && _game.value.status == GameStatus.ACTIVE) startClock()
    }

    fun clearResults() {
        viewModelScope.launch(Dispatchers.IO) { gameRepository.clearResults() }
    }

    fun setAppearance(value: AppearanceMode) {
        viewModelScope.launch { settingsRepository.setAppearance(value) }
    }

    fun setGameLayout(value: GameLayout) {
        viewModelScope.launch { settingsRepository.setGameLayout(value) }
    }

    fun setThemeHue(value: Float) {
        viewModelScope.launch { settingsRepository.setThemeHue(value) }
    }

    fun setFullscreen(value: Boolean) {
        viewModelScope.launch { settingsRepository.setFullscreen(value) }
    }

    fun setSound(value: Boolean) {
        viewModelScope.launch { settingsRepository.setSound(value) }
    }

    fun setVibration(value: Boolean) {
        viewModelScope.launch { settingsRepository.setVibration(value) }
    }

    fun setFlagModeDefault(value: Boolean) {
        isFlagMode.value = value
        viewModelScope.launch { settingsRepository.setFlagModeDefault(value) }
    }

    fun saveCustomBoard(preset: CustomBoardPreset) {
        viewModelScope.launch { settingsRepository.saveCustomBoard(preset) }
    }

    fun deleteCustomBoard(preset: CustomBoardPreset) {
        viewModelScope.launch { settingsRepository.deleteCustomBoard(preset) }
    }

    private fun updateGame(updated: GameState, normalSound: SoundEffect) {
        val completed = updated.status == GameStatus.WON || updated.status == GameStatus.LOST
        if (completed) stopClock()
        _game.value = updated
        publishDisplayElapsed(updated.elapsedMillis)
        resultDialogVisible.value = completed
        if (settings.value.soundEnabled) {
            _soundEffects.tryEmit(
                when (updated.status) {
                    GameStatus.WON -> SoundEffect.WIN
                    GameStatus.LOST -> SoundEffect.LOSE
                    else -> normalSound
                },
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (completed) {
                gameRepository.addResult(
                    updated,
                    if (updated.status == GameStatus.WON) ResultType.WON else ResultType.LOST,
                )
                gameRepository.clearActiveGame()
            } else {
                gameRepository.saveActiveGame(updated)
            }
        }
    }

    private fun withCurrentElapsed(state: GameState, now: Long): GameState {
        if (state.status != GameStatus.ACTIVE) return state
        return state.copy(elapsedMillis = elapsedAt(state.elapsedMillis, runningSinceRealtime, now))
    }

    private fun startClock() {
        if (runningSinceRealtime == null) runningSinceRealtime = SystemClock.elapsedRealtime()
        startTimer()
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = withCurrentElapsed(_game.value, SystemClock.elapsedRealtime()).elapsedMillis
                publishDisplayElapsed(elapsed)
                val untilNextSecond = 1_000L - elapsed % 1_000L
                delay(untilNextSecond)
            }
        }
    }

    private fun stopClock() {
        runningSinceRealtime = null
        timerJob?.cancel()
        timerJob = null
    }

    private fun publishDisplayElapsed(elapsedMillis: Long) {
        _displayElapsedMillis.value = elapsedMillis.coerceAtLeast(0L) / 1_000L * 1_000L
    }
}

internal fun elapsedAt(elapsedMillis: Long, startedAt: Long?, now: Long): Long =
    elapsedMillis + if (startedAt == null) 0L else (now - startedAt).coerceAtLeast(0L)

fun GameResult.shareText(locale: Locale = Locale.getDefault()): String {
    val time = formatDuration(elapsedMillis)
    val date = SimpleDateFormat("yyyy/MM/dd HH:mm", locale).format(Date(endedAtMillis))
    return """踩地雷

結果：${resultType.label}
難度：${difficulty.label}
棋盤：$width × $height
地雷：$mineCount
時間：$time
日期：$date"""
}

fun formatDuration(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis.coerceAtLeast(0L) / 1_000
    val hours = totalSeconds / 3_600
    val minutes = totalSeconds % 3_600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
