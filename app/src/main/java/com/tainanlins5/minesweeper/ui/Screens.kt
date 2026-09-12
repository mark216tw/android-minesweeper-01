package com.tainanlins5.minesweeper.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tainanlins5.minesweeper.BuildConfig
import com.tainanlins5.minesweeper.GameViewModel
import com.tainanlins5.minesweeper.R
import com.tainanlins5.minesweeper.data.AppSettings
import com.tainanlins5.minesweeper.data.AppearanceMode
import com.tainanlins5.minesweeper.data.CustomBoardPreset
import com.tainanlins5.minesweeper.data.GameResult
import com.tainanlins5.minesweeper.data.GameLayout
import com.tainanlins5.minesweeper.data.SettingsRepository
import com.tainanlins5.minesweeper.domain.Difficulty
import com.tainanlins5.minesweeper.domain.GameState
import com.tainanlins5.minesweeper.domain.GameStatus
import com.tainanlins5.minesweeper.domain.MinesweeperEngine
import com.tainanlins5.minesweeper.domain.ResultType
import com.tainanlins5.minesweeper.formatDuration
import com.tainanlins5.minesweeper.shareText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.StateFlow

private data class GameConfig(val difficulty: Difficulty, val width: Int, val height: Int, val mines: Int)

private data class GameLayoutActions(
    val onRecords: () -> Unit,
    val onFullscreen: () -> Unit,
    val onSettings: () -> Unit,
    val onExit: () -> Unit,
    val onRestart: () -> Unit,
    val onFlagMode: () -> Unit,
    val onCenter: () -> Unit,
    val onNewGame: () -> Unit,
    val onReveal: (Int) -> Unit,
    val onToggleFlag: (Int) -> Unit,
    val onLongPress: () -> Unit,
)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    game: GameState,
    settings: AppSettings,
    colors: RetroColors,
    flagMode: Boolean,
    resultDialogVisible: Boolean,
    onMoveToBackground: () -> Unit,
) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val haptic = LocalHapticFeedback.current
    var showNewGame by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }
    var pendingGame by remember { mutableStateOf<GameConfig?>(null) }
    var resetToken by remember { mutableIntStateOf(0) }

    fun launch(config: GameConfig) {
        if (game.status == GameStatus.ACTIVE) pendingGame = config
        else viewModel.startGame(config.difficulty, config.width, config.height, config.mines)
    }

    val actions = GameLayoutActions(
        onRecords = { viewModel.showScreen(com.tainanlins5.minesweeper.AppScreen.RECORDS) },
        onFullscreen = { viewModel.setFullscreen(!settings.fullscreen) },
        onSettings = { viewModel.showScreen(com.tainanlins5.minesweeper.AppScreen.SETTINGS) },
        onExit = onMoveToBackground,
        onRestart = { launch(GameConfig(game.difficulty, game.width, game.height, game.mineCount)) },
        onFlagMode = viewModel::toggleFlagMode,
        onCenter = { resetToken++ },
        onNewGame = { showNewGame = true },
        onReveal = viewModel::reveal,
        onToggleFlag = viewModel::toggleFlag,
        onLongPress = {
            if (settings.vibrationEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
    )

    when (settings.gameLayout) {
        GameLayout.CLASSIC -> ClassicGameLayout(
            game = game,
            colors = colors,
            flagMode = flagMode,
            fullscreen = settings.fullscreen,
            elapsedMillis = viewModel.displayElapsedMillis,
            resetToken = resetToken,
            landscape = landscape,
            actions = actions,
        )
        GameLayout.HANDHELD -> HandheldGameLayout(
            game = game,
            colors = colors,
            flagMode = flagMode,
            fullscreen = settings.fullscreen,
            elapsedMillis = viewModel.displayElapsedMillis,
            resetToken = resetToken,
            landscape = landscape,
            actions = actions,
        )
    }

    if (showNewGame) {
        NewGameDialog(
            colors = colors,
            onDismiss = { showNewGame = false },
            onDifficulty = { difficulty ->
                showNewGame = false
                launch(GameConfig(difficulty, difficulty.width, difficulty.height, difficulty.mines))
            },
            onCustom = {
                showNewGame = false
                showCustom = true
            },
        )
    }
    if (showCustom) {
        CustomGameDialog(
            colors = colors,
            presets = settings.customBoards,
            onDismiss = { showCustom = false },
            onSave = viewModel::saveCustomBoard,
            onDelete = viewModel::deleteCustomBoard,
            onStart = { width, height, mines ->
                showCustom = false
                launch(GameConfig(Difficulty.CUSTOM, width, height, mines))
            },
        )
    }
    pendingGame?.let { config ->
        ConfirmDialog(
            colors = colors,
            title = "放棄目前遊戲？",
            text = "目前進度將記錄為放棄。",
            confirmText = "放棄並開始",
            onDismiss = { pendingGame = null },
            onConfirm = {
                pendingGame = null
                viewModel.startGame(config.difficulty, config.width, config.height, config.mines)
            },
        )
    }
    if (resultDialogVisible && (game.status == GameStatus.WON || game.status == GameStatus.LOST)) {
        val won = game.status == GameStatus.WON
        AlertDialog(
            onDismissRequest = viewModel::dismissResultDialog,
            containerColor = colors.panel,
            title = { Text(if (won) "掃雷成功！" else "踩到地雷！", color = colors.text, fontWeight = FontWeight.Black) },
            text = { Text("時間：${formatDuration(game.elapsedMillis)}", color = colors.text) },
            confirmButton = {
                RetroButton("再玩一次", colors, onClick = {
                    viewModel.dismissResultDialog()
                    viewModel.restartGame()
                })
            },
            dismissButton = { RetroButton("關閉", colors, onClick = viewModel::dismissResultDialog) },
        )
    }
}

@Composable
private fun ClassicGameLayout(
    game: GameState,
    colors: RetroColors,
    flagMode: Boolean,
    fullscreen: Boolean,
    elapsedMillis: StateFlow<Long>,
    resetToken: Int,
    landscape: Boolean,
    actions: GameLayoutActions,
) {
    Column(Modifier.fillMaxSize().background(colors.desktop).padding(8.dp)) {
        AppHeader(colors, fullscreen, actions.onRecords, actions.onFullscreen, actions.onSettings, actions.onExit)
        Spacer(Modifier.height(8.dp))
        RetroPanel(colors, Modifier.fillMaxSize()) {
            if (landscape) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(
                        Modifier.width(190.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatusPanel(game.remainingMines, game.status, elapsedMillis, colors, actions.onRestart)
                        GameControls(
                            colors,
                            flagMode,
                            actions.onFlagMode,
                            actions.onCenter,
                            actions.onNewGame,
                            vertical = true,
                        )
                    }
                    SharedGameBoard(game, colors, flagMode, resetToken, actions, Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusPanel(game.remainingMines, game.status, elapsedMillis, colors, actions.onRestart)
                    SharedGameBoard(game, colors, flagMode, resetToken, actions, Modifier.weight(1f).fillMaxWidth())
                    GameControls(
                        colors,
                        flagMode,
                        actions.onFlagMode,
                        actions.onCenter,
                        actions.onNewGame,
                        vertical = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun HandheldGameLayout(
    game: GameState,
    colors: RetroColors,
    flagMode: Boolean,
    fullscreen: Boolean,
    elapsedMillis: StateFlow<Long>,
    resetToken: Int,
    landscape: Boolean,
    actions: GameLayoutActions,
) {
    if (landscape) {
        Row(
            Modifier.fillMaxSize().background(colors.desktop).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            RetroPanel(colors, Modifier.weight(1f).fillMaxHeight()) {
                SharedGameBoard(game, colors, flagMode, resetToken, actions, Modifier.fillMaxSize())
            }
            Column(
                Modifier.width(216.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                HandheldHeader(colors, fullscreen, actions, stacked = true)
                RetroPanel(colors, Modifier.weight(1f).fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        HandheldSideStatus(game, elapsedMillis, colors, actions.onRestart)
                        HandheldControls(colors, flagMode, actions, vertical = true)
                    }
                }
            }
        }
    } else {
        Column(
            Modifier.fillMaxSize().background(colors.desktop).padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            HandheldHeader(colors, fullscreen, actions, stacked = false)
            RetroPanel(colors, Modifier.fillMaxWidth()) {
                StatusPanel(game.remainingMines, game.status, elapsedMillis, colors, actions.onRestart)
            }
            RetroPanel(colors, Modifier.weight(1f).fillMaxWidth()) {
                SharedGameBoard(game, colors, flagMode, resetToken, actions, Modifier.fillMaxSize())
            }
            HandheldControls(colors, flagMode, actions, vertical = false)
        }
    }
}

@Composable
private fun SharedGameBoard(
    game: GameState,
    colors: RetroColors,
    flagMode: Boolean,
    resetToken: Int,
    actions: GameLayoutActions,
    modifier: Modifier,
) {
    GameBoard(
        width = game.width,
        height = game.height,
        cells = game.cells,
        status = game.status,
        colors = colors,
        flagMode = flagMode,
        resetToken = resetToken,
        onReveal = actions.onReveal,
        onToggleFlag = actions.onToggleFlag,
        onLongPress = actions.onLongPress,
        modifier = modifier.sunkenBorder(colors),
    )
}

@Composable
private fun HandheldHeader(
    colors: RetroColors,
    fullscreen: Boolean,
    actions: GameLayoutActions,
    stacked: Boolean,
) {
    if (stacked) {
        Column(
            Modifier.fillMaxWidth().background(colors.panel).raisedBorder(colors).padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("踩地雷", color = colors.text, fontWeight = FontWeight.Black, fontSize = 19.sp)
            Text("POCKET SWEEPER", color = colors.mutedText, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            Spacer(Modifier.height(5.dp))
            HandheldToolButtons(colors, fullscreen, actions)
        }
    } else {
        Row(
            Modifier.fillMaxWidth().background(colors.panel).raisedBorder(colors).padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("踩地雷", color = colors.text, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("POCKET SWEEPER", color = colors.mutedText, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
            HandheldToolButtons(colors, fullscreen, actions)
        }
    }
}

@Composable
private fun HandheldToolButtons(colors: RetroColors, fullscreen: Boolean, actions: GameLayoutActions) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        RetroIconButton(
            R.drawable.ic_history,
            contentDescription = "遊戲紀錄",
            colors = colors,
            onClick = actions.onRecords,
            modifier = Modifier.size(48.dp),
        )
        RetroIconButton(
            R.drawable.ic_fullscreen,
            contentDescription = if (fullscreen) "關閉全螢幕" else "開啟全螢幕",
            colors = colors,
            onClick = actions.onFullscreen,
            modifier = Modifier.size(48.dp),
            selected = fullscreen,
        )
        RetroIconButton(
            R.drawable.ic_settings,
            contentDescription = "設定",
            colors = colors,
            onClick = actions.onSettings,
            modifier = Modifier.size(48.dp),
        )
        RetroIconButton(
            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
            contentDescription = "將應用程式移至背景",
            colors = colors,
            onClick = actions.onExit,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun HandheldSideStatus(
    game: GameState,
    elapsedMillis: StateFlow<Long>,
    colors: RetroColors,
    onRestart: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("剩餘地雷", color = colors.mutedText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        SevenSegmentDisplay(game.remainingMines.coerceIn(-99, 999).toString().padStart(3, '0'), colors)
        Spacer(Modifier.height(7.dp))
        RetroButton(
            text = when (game.status) {
                GameStatus.READY -> "🙂"
                GameStatus.ACTIVE -> "😐"
                GameStatus.WON -> "😎"
                GameStatus.LOST -> "😵"
            },
            colors = colors,
            onClick = onRestart,
            modifier = Modifier.size(52.dp).semantics { contentDescription = "重新開始遊戲" },
        )
        Spacer(Modifier.height(7.dp))
        Text("遊戲時間", color = colors.mutedText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        ElapsedTimeDisplay(elapsedMillis, colors)
        Spacer(Modifier.height(5.dp))
        Text(
            "${game.difficulty.label} · ${game.width}×${game.height}",
            color = colors.mutedText,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun HandheldControls(
    colors: RetroColors,
    flagMode: Boolean,
    actions: GameLayoutActions,
    vertical: Boolean,
) {
    val content: @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            HandheldRoundButton("A", "插旗", colors, flagMode, actions.onFlagMode)
            HandheldRoundButton("B", "置中", colors, false, actions.onCenter)
        }
        HandheldStartButton(colors, actions.onNewGame)
    }
    if (vertical) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) { content() }
    } else {
        Row(
            Modifier.fillMaxWidth().background(colors.panel).raisedBorder(colors).padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) { content() }
    }
}

@Composable
private fun HandheldRoundButton(
    code: String,
    label: String,
    colors: RetroColors,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) colors.accent else colors.panelDark
    val foreground = if (selected) colors.onAccent else colors.panelLight
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .semantics {
                contentDescription = label
                role = Role.Button
                this.selected = selected
            }
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .size(54.dp)
                .background(background, CircleShape)
                .border(3.dp, if (selected) colors.panelLight else colors.panelDark, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(code, color = foreground, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
        Text(
            if (selected) "✓ $label" else label,
            color = if (selected) colors.accent else colors.text,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun HandheldStartButton(colors: RetroColors, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .semantics {
                contentDescription = "新遊戲"
                role = Role.Button
            }
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .width(88.dp)
                .height(30.dp)
                .background(colors.panelDark, RoundedCornerShape(15.dp))
                .border(2.dp, colors.panelLight, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("START", color = colors.panelLight, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
        Text("新遊戲", color = colors.text, fontWeight = FontWeight.Black, fontSize = 11.sp)
    }
}

@Composable
private fun AppHeader(
    colors: RetroColors,
    fullscreen: Boolean,
    onRecords: () -> Unit,
    onFullscreen: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("踩地雷", color = colors.text, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text("MINESWEEPER", color = colors.mutedText, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
        RetroIconButton(
            R.drawable.ic_history,
            contentDescription = "遊戲紀錄",
            colors = colors,
            onClick = onRecords,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.width(6.dp))
        RetroIconButton(
            R.drawable.ic_fullscreen,
            contentDescription = if (fullscreen) "關閉全螢幕" else "開啟全螢幕",
            colors = colors,
            onClick = onFullscreen,
            modifier = Modifier.size(48.dp),
            selected = fullscreen,
        )
        Spacer(Modifier.width(6.dp))
        RetroIconButton(
            R.drawable.ic_settings,
            contentDescription = "設定",
            colors = colors,
            onClick = onSettings,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.width(6.dp))
        RetroIconButton(
            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
            contentDescription = "將應用程式移至背景",
            colors = colors,
            onClick = onExit,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun StatusPanel(
    remainingMines: Int,
    status: GameStatus,
    elapsedMillis: StateFlow<Long>,
    colors: RetroColors,
    onRestart: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().background(colors.panel).raisedBorder(colors).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SevenSegmentDisplay(remainingMines.coerceIn(-99, 999).toString().padStart(3, '0'), colors)
        RetroButton(
            text = when (status) {
                GameStatus.READY -> "🙂"
                GameStatus.ACTIVE -> "😐"
                GameStatus.WON -> "😎"
                GameStatus.LOST -> "😵"
            },
            colors = colors,
            onClick = onRestart,
            modifier = Modifier.size(52.dp),
        )
        ElapsedTimeDisplay(elapsedMillis, colors)
    }
}

@Composable
private fun ElapsedTimeDisplay(elapsedMillis: StateFlow<Long>, colors: RetroColors) {
    val elapsed by elapsedMillis.collectAsStateWithLifecycle()
    SevenSegmentDisplay(formatDuration(elapsed), colors)
}

@Composable
private fun GameControls(
    colors: RetroColors,
    flagMode: Boolean,
    onFlagMode: () -> Unit,
    onCenter: () -> Unit,
    onNewGame: () -> Unit,
    vertical: Boolean,
) {
    if (vertical) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RetroButton("插旗模式", colors, onFlagMode, Modifier.fillMaxWidth(), selected = flagMode)
            RetroButton("置中棋盤", colors, onCenter, Modifier.fillMaxWidth())
            RetroButton("新遊戲", colors, onNewGame, Modifier.fillMaxWidth())
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RetroButton("插旗", colors, onFlagMode, Modifier.weight(1f), selected = flagMode)
            RetroButton("置中", colors, onCenter, Modifier.weight(1f))
            RetroButton("新遊戲", colors, onNewGame, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NewGameDialog(
    colors: RetroColors,
    onDismiss: () -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onCustom: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.panel,
        title = { Text("選擇難度", color = colors.text, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Difficulty.BEGINNER, Difficulty.INTERMEDIATE, Difficulty.EXPERT).forEach { difficulty ->
                    RetroButton(
                        "${difficulty.label}  ${difficulty.width} × ${difficulty.height} / ${difficulty.mines} 雷",
                        colors,
                        onClick = { onDifficulty(difficulty) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                RetroButton("自訂棋盤", colors, onClick = onCustom, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {},
        dismissButton = { RetroButton("取消", colors, onDismiss) },
    )
}

@Composable
private fun CustomGameDialog(
    colors: RetroColors,
    presets: List<CustomBoardPreset>,
    onDismiss: () -> Unit,
    onSave: (CustomBoardPreset) -> Unit,
    onDelete: (CustomBoardPreset) -> Unit,
    onStart: (Int, Int, Int) -> Unit,
) {
    val context = LocalContext.current
    var width by remember { mutableIntStateOf(16) }
    var height by remember { mutableIntStateOf(16) }
    var mines by remember { mutableIntStateOf(40) }
    val maxMines = width * height - 9
    if (mines > maxMines) mines = maxMines
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.panel,
        title = { Text("自訂棋盤", color = colors.text, fontWeight = FontWeight.Black) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (presets.isNotEmpty()) {
                    Text("已儲存 (${presets.size}/${SettingsRepository.MAX_CUSTOM_BOARDS})", color = colors.text, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(6.dp))
                    presets.forEach { preset ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RetroButton(
                                preset.label,
                                colors,
                                onClick = {
                                    width = preset.width
                                    height = preset.height
                                    mines = preset.mines
                                },
                                modifier = Modifier.weight(1f),
                                selected = preset == CustomBoardPreset(width, height, mines),
                            )
                            RetroButton("刪除", colors, onClick = { onDelete(preset) })
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                } else {
                    Text("尚未儲存自訂棋盤 (0/${SettingsRepository.MAX_CUSTOM_BOARDS})", color = colors.mutedText)
                    Spacer(Modifier.height(6.dp))
                }
                NumberSlider("寬度", width, MinesweeperEngine.MIN_SIZE..MinesweeperEngine.MAX_WIDTH, colors) { width = it }
                NumberSlider("高度", height, MinesweeperEngine.MIN_SIZE..MinesweeperEngine.MAX_HEIGHT, colors) { height = it }
                NumberSlider("地雷", mines, 1..maxMines, colors) { mines = it }
                val density = mines * 100f / (width * height)
                Text("地雷密度：%.1f%%".format(density), color = colors.mutedText)
                Spacer(Modifier.height(8.dp))
                RetroButton(
                    "儲存設定 (${presets.size}/${SettingsRepository.MAX_CUSTOM_BOARDS})",
                    colors,
                    onClick = {
                        val preset = CustomBoardPreset(width, height, mines)
                        when {
                            preset in presets -> Toast.makeText(context, "此棋盤已儲存", Toast.LENGTH_SHORT).show()
                            presets.size >= SettingsRepository.MAX_CUSTOM_BOARDS -> Toast.makeText(context, "最多只能儲存 3 個自訂棋盤", Toast.LENGTH_SHORT).show()
                            else -> {
                                onSave(preset)
                                Toast.makeText(context, "自訂棋盤已儲存", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { RetroButton("開始", colors, onClick = { onStart(width, height, mines) }, selected = true) },
        dismissButton = { RetroButton("取消", colors, onDismiss) },
    )
}

@Composable
private fun NumberSlider(label: String, value: Int, range: IntRange, colors: RetroColors, onValue: (Int) -> Unit) {
    Text("$label：$value", color = colors.text, fontWeight = FontWeight.Bold)
    Slider(
        value = value.toFloat(),
        onValueChange = { onValue(it.roundToInt().coerceIn(range)) },
        valueRange = range.first.toFloat()..range.last.toFloat(),
        colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent),
    )
}

@Composable
fun RecordsScreen(viewModel: GameViewModel, results: List<GameResult>, colors: RetroColors) {
    val context = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(colors.desktop).padding(8.dp)) {
        ScreenHeader("遊戲紀錄", colors, onBack = { viewModel.showScreen(com.tainanlins5.minesweeper.AppScreen.GAME) })
        Spacer(Modifier.height(8.dp))
        if (results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("尚無遊戲紀錄", color = colors.mutedText, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results, key = GameResult::id) { result ->
                    ResultCard(
                        result,
                        colors,
                        onCopy = { copyResult(context, result) },
                        onShare = { shareResult(context, result) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            RetroButton("清除全部紀錄", colors, onClick = { confirmClear = true }, modifier = Modifier.fillMaxWidth())
        }
    }
    if (confirmClear) {
        ConfirmDialog(
            colors,
            title = "清除全部紀錄？",
            text = "此動作無法復原，但不會影響目前遊戲與設定。",
            confirmText = "清除",
            onDismiss = { confirmClear = false },
            onConfirm = {
                confirmClear = false
                viewModel.clearResults()
            },
        )
    }
}

@Composable
private fun ResultCard(result: GameResult, colors: RetroColors, onCopy: () -> Unit, onShare: () -> Unit) {
    val dark = colors.desktop.luminanceForUi() < 0.5f
    val visual = resultVisual(result.resultType, dark)
    RetroPanel(colors, Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(visual.background))
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        Modifier.background(visual.background).padding(horizontal = 9.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(visual.iconRes),
                            contentDescription = null,
                            tint = visual.foreground,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            result.resultType.label,
                            color = visual.foreground,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                        )
                    }
                    Text(
                        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(result.endedAtMillis)),
                        color = colors.mutedText,
                    )
                }
                Spacer(Modifier.height(7.dp))
                Text("${result.difficulty.label}　${result.width} × ${result.height}　${result.mineCount} 雷", color = colors.text)
                Text("時間 ${formatDuration(result.elapsedMillis)}", color = colors.text, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RetroButton("複製", colors, onCopy, Modifier.weight(1f))
                    RetroButton("分享", colors, onShare, Modifier.weight(1f), selected = true)
                }
            }
        }
    }
}

private data class ResultVisual(
    val iconRes: Int,
    val background: Color,
    val foreground: Color,
)

private fun resultVisual(resultType: ResultType, dark: Boolean): ResultVisual = when (resultType) {
    ResultType.WON -> ResultVisual(
        R.drawable.ic_result_won,
        if (dark) Color(0xFF237A35) else Color(0xFF2E7D32),
        Color.White,
    )
    ResultType.LOST -> ResultVisual(
        R.drawable.ic_result_lost,
        if (dark) Color(0xFFC93B3B) else Color(0xFFC62828),
        Color.White,
    )
    ResultType.ABANDONED -> ResultVisual(
        R.drawable.ic_result_abandoned,
        if (dark) Color(0xFFB87800) else Color(0xFFF9A825),
        if (dark) Color.White else Color(0xFF171000),
    )
}

@Composable
fun SettingsScreen(viewModel: GameViewModel, settings: AppSettings, colors: RetroColors) {
    var showHelp by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().background(colors.desktop).verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScreenHeader("設定", colors, onBack = { viewModel.showScreen(com.tainanlins5.minesweeper.AppScreen.GAME) })
        RetroPanel(colors, Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("主介面版型", colors)
                ChoiceRow(
                    GameLayout.entries,
                    settings.gameLayout,
                    GameLayout::label,
                    colors,
                    viewModel::setGameLayout,
                    Modifier.fillMaxWidth(),
                )
                Text("切換版型不會中斷目前遊戲", color = colors.mutedText, fontSize = 12.sp)
            }
        }
        RetroPanel(colors, Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("外觀模式", colors)
                ChoiceRow(
                    AppearanceMode.entries,
                    settings.appearanceMode,
                    AppearanceMode::label,
                    colors,
                    viewModel::setAppearance,
                    Modifier.fillMaxWidth(),
                )
            }
        }
        ThemeSettings(settings, colors, viewModel::setThemeHue)
        RetroPanel(colors, Modifier.fillMaxWidth()) {
            Column {
                SettingSwitch("遊戲音效", settings.soundEnabled, colors, viewModel::setSound)
                SettingSwitch("震動回饋", settings.vibrationEnabled, colors, viewModel::setVibration)
                SettingSwitch("預設插旗模式", settings.flagModeDefault, colors, viewModel::setFlagModeDefault)
            }
        }
        RetroButton("玩法說明", colors, onClick = { showHelp = true }, modifier = Modifier.fillMaxWidth())
        Text("踩地雷 ${BuildConfig.VERSION_NAME}", color = colors.mutedText, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            containerColor = colors.panel,
            title = { Text("玩法說明", color = colors.text, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "點按開啟格子，長按插旗。數字表示周圍八格的地雷數。旗幟數與已開啟數字相同時，點擊該數字可快速展開周圍格子。開啟所有安全格即可獲勝。",
                    color = colors.text,
                )
            },
            confirmButton = { RetroButton("知道了", colors, onClick = { showHelp = false }, selected = true) },
        )
    }
}

private data class HuePreset(val name: String, val hue: Float)

private val HuePresets = listOf(
    HuePreset("活力紅", 4f),
    HuePreset("陽光黃", 46f),
    HuePreset("青檸綠", 92f),
    HuePreset("湖水青", 172f),
    HuePreset("天空藍", 210f),
    HuePreset("葡萄紫", 278f),
)

@Composable
private fun ThemeSettings(settings: AppSettings, colors: RetroColors, onHue: (Float) -> Unit) {
    val matched = HuePresets.firstOrNull { kotlin.math.abs(it.hue - settings.themeHue) < 1f }
    RetroPanel(colors, Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("主題色${matched?.let { " · ${it.name}" } ?: " · 自訂"}", colors)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HuePresets.forEach { preset ->
                    val color = colorFromHue(preset.hue, false)
                    Box(
                        Modifier
                            .size(42.dp)
                            .background(color, CircleShape)
                            .border(if (matched == preset) 4.dp else 1.dp, if (matched == preset) colors.text else colors.panelDark, CircleShape)
                            .clickable { onHue(preset.hue) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (matched == preset) Text("✓", color = if (color.luminanceForUi() > .5f) Color.Black else Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
            Text("拖曳色彩滑桿即可即時套用", color = colors.mutedText)
            Slider(
                value = settings.themeHue,
                onValueChange = onHue,
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.panelDark,
                ),
            )
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, colors: RetroColors, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChecked(!checked) }.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.text, fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        if (checked) Text("✓", color = colors.accent, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(6.dp))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun ScreenHeader(title: String, colors: RetroColors, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RetroButton("‹ 返回", colors, onBack)
        Spacer(Modifier.width(12.dp))
        Text(title, color = colors.text, fontWeight = FontWeight.Black, fontSize = 23.sp)
    }
}

@Composable
private fun ConfirmDialog(
    colors: RetroColors,
    title: String,
    text: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.panel,
        title = { Text(title, color = colors.text, fontWeight = FontWeight.Black) },
        text = { Text(text, color = colors.text) },
        confirmButton = { RetroButton(confirmText, colors, onConfirm, selected = true) },
        dismissButton = { RetroButton("取消", colors, onDismiss) },
    )
}

private fun copyResult(context: Context, result: GameResult) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("踩地雷遊戲結果", result.shareText()))
    Toast.makeText(context, "遊戲結果已複製", Toast.LENGTH_SHORT).show()
}

private fun shareResult(context: Context, result: GameResult) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "踩地雷遊戲結果")
        putExtra(Intent.EXTRA_TEXT, result.shareText())
    }
    context.startActivity(Intent.createChooser(intent, "分享遊戲結果"))
}

private fun Color.luminanceForUi(): Float = red * 0.299f + green * 0.587f + blue * 0.114f
