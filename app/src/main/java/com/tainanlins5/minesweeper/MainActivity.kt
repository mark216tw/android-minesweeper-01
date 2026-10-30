package com.tainanlins5.minesweeper

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tainanlins5.minesweeper.domain.GameStatus
import com.tainanlins5.minesweeper.ui.GameScreen
import com.tainanlins5.minesweeper.ui.MinesweeperTheme
import com.tainanlins5.minesweeper.ui.RecordsScreen
import com.tainanlins5.minesweeper.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val game by viewModel.game.collectAsStateWithLifecycle()
            val results by viewModel.results.collectAsStateWithLifecycle()
            val screen by viewModel.screen.collectAsStateWithLifecycle()
            val flagMode by viewModel.isFlagMode.collectAsStateWithLifecycle()
            val loaded by viewModel.isLoaded.collectAsStateWithLifecycle()
            val resultDialogVisible by viewModel.resultDialogVisible.collectAsStateWithLifecycle()

            LaunchedEffect(loaded, screen, game.status) {
                window.decorView.keepScreenOn =
                    loaded && screen == AppScreen.GAME && game.status == GameStatus.ACTIVE
            }

            BackHandler(enabled = screen != AppScreen.GAME) {
                viewModel.showScreen(AppScreen.GAME)
            }

            MinesweeperTheme(settings) { colors, dark ->
                LaunchedEffect(colors.desktop, dark) {
                    val barColor = colors.desktop.toArgb()
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(barColor, barColor) { dark },
                        navigationBarStyle = SystemBarStyle.auto(barColor, barColor) { dark },
                    )
                    window.disableNavigationBarContrast()
                }

                LaunchedEffect(settings.fullscreen, dark) {
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    controller.isAppearanceLightStatusBars = !dark
                    controller.isAppearanceLightNavigationBars = !dark
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    if (settings.fullscreen) {
                        controller.hide(WindowInsetsCompat.Type.systemBars())
                    } else {
                        controller.show(WindowInsetsCompat.Type.systemBars())
                    }
                }

                if (!loaded) {
                    Box(
                        Modifier.fillMaxSize().background(colors.desktop),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("載入中…", color = colors.text)
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(colors.desktop)
                            .then(
                                if (settings.fullscreen) Modifier
                                else Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                            ),
                    ) {
                        when (screen) {
                            AppScreen.GAME -> GameScreen(
                                viewModel,
                                game,
                                settings,
                                colors,
                                flagMode,
                                resultDialogVisible,
                            )
                            AppScreen.RECORDS -> RecordsScreen(viewModel, results, colors)
                            AppScreen.SETTINGS -> SettingsScreen(viewModel, settings, colors)
                        }
                    }
                }
            }

            val lifecycle = lifecycle
            DisposableEffect(lifecycle) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> viewModel.onAppResumed()
                        Lifecycle.Event.ON_PAUSE -> viewModel.onAppPaused()
                        else -> Unit
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            LaunchedEffect(Unit) {
                val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55)
                try {
                    viewModel.soundEffects.collect { effect ->
                        val toneType = when (effect) {
                            SoundEffect.REVEAL -> ToneGenerator.TONE_PROP_BEEP
                            SoundEffect.FLAG -> ToneGenerator.TONE_PROP_ACK
                            SoundEffect.WIN -> ToneGenerator.TONE_PROP_PROMPT
                            SoundEffect.LOSE -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                        }
                        tone.startTone(toneType, if (effect == SoundEffect.LOSE) 350 else 90)
                    }
                } finally {
                    tone.release()
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun Window.disableNavigationBarContrast() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isNavigationBarContrastEnforced = false
    }
}
