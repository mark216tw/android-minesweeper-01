package com.tainanlins5.minesweeper

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tainanlins5.minesweeper.data.AppSettings
import com.tainanlins5.minesweeper.data.AppearanceMode
import com.tainanlins5.minesweeper.data.CustomBoardPreset
import com.tainanlins5.minesweeper.data.GameResult
import com.tainanlins5.minesweeper.domain.Difficulty
import com.tainanlins5.minesweeper.domain.GameState
import com.tainanlins5.minesweeper.domain.GameStatus
import com.tainanlins5.minesweeper.domain.MinesweeperEngine
import com.tainanlins5.minesweeper.domain.ResultType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    val game = MutableStateFlow(MinesweeperEngine.newGame(Difficulty.BEGINNER))
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val restored = gameRepository.loadActiveGame()
            if (restored != null) game.value = restored
            if (game.value.status == GameStatus.ACTIVE) runningSinceRealtime = SystemClock.elapsedRealtime()
            isFlagMode.value = settingsRepository.settings.first().flagModeDefault
            isLoaded.value = true
        }
        viewModelScope.launch {
            while (true) {
                delay(250)
                val state = game.value
                if (state.status == GameStatus.ACTIVE && runningSinceRealtime != null) {
                    game.value = withCurrentElapsed(state)
                }
            }
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
        val before = withCurrentElapsed(game.value)
        if (before.status == GameStatus.WON || before.status == GameStatus.LOST) return
        val after = if (before.cells.getOrNull(index)?.isRevealed == true) {
            MinesweeperEngine.chord(before, index)
        } else {
            MinesweeperEngine.reveal(before, index)
        }
        if (after == before) return
        if (before.status == GameStatus.READY && after.status == GameStatus.ACTIVE) {
            runningSinceRealtime = SystemClock.elapsedRealtime()
        }
        updateGame(after, SoundEffect.REVEAL)
    }

    fun toggleFlag(index: Int) {
        val before = withCurrentElapsed(game.value)
        val after = MinesweeperEngine.toggleFlag(before, index)
        if (after == before) return
        updateGame(after, SoundEffect.FLAG)
    }

    fun toggleFlagMode() {
        isFlagMode.value = !isFlagMode.value
    }

    fun startGame(difficulty: Difficulty, width: Int = difficulty.width, height: Int = difficulty.height, mines: Int = difficulty.mines) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = withCurrentElapsed(game.value)
            if (current.hasStarted && current.status == GameStatus.ACTIVE) {
                gameRepository.addResult(current, ResultType.ABANDONED)
            }
            runningSinceRealtime = null
            resultDialogVisible.value = false
            game.value = MinesweeperEngine.newGame(difficulty, width, height, mines)
            gameRepository.saveActiveGame(game.value)
            screen.value = AppScreen.GAME
        }
    }

    fun restartGame() = startGame(
        game.value.difficulty,
        game.value.width,
        game.value.height,
        game.value.mineCount,
    )

    fun onAppPaused() {
        pauseClock()
    }

    private fun pauseClock() {
        val state = withCurrentElapsed(game.value)
        game.value = state
        runningSinceRealtime = null
        if (state.status == GameStatus.READY || state.status == GameStatus.ACTIVE) {
            viewModelScope.launch(Dispatchers.IO) { gameRepository.saveActiveGame(state) }
        }
    }

    fun onAppResumed() {
        if (game.value.status == GameStatus.ACTIVE && runningSinceRealtime == null) {
            runningSinceRealtime = SystemClock.elapsedRealtime()
        }
    }

    fun clearResults() {
        viewModelScope.launch(Dispatchers.IO) { gameRepository.clearResults() }
    }

    fun setAppearance(value: AppearanceMode) {
        viewModelScope.launch { settingsRepository.setAppearance(value) }
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
        val finalState = if (completed) withCurrentElapsed(updated) else updated
        game.value = finalState
        resultDialogVisible.value = completed
        if (settings.value.soundEnabled) {
            _soundEffects.tryEmit(
                when (finalState.status) {
                    GameStatus.WON -> SoundEffect.WIN
                    GameStatus.LOST -> SoundEffect.LOSE
                    else -> normalSound
                },
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (completed) {
                runningSinceRealtime = null
                gameRepository.addResult(
                    finalState,
                    if (finalState.status == GameStatus.WON) ResultType.WON else ResultType.LOST,
                )
                gameRepository.clearActiveGame()
            } else {
                gameRepository.saveActiveGame(finalState)
            }
        }
    }

    private fun withCurrentElapsed(state: GameState): GameState {
        val started = runningSinceRealtime ?: return state
        if (state.status != GameStatus.ACTIVE) return state
        val now = SystemClock.elapsedRealtime()
        runningSinceRealtime = now
        return state.copy(elapsedMillis = state.elapsedMillis + (now - started).coerceAtLeast(0L))
    }
}

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
