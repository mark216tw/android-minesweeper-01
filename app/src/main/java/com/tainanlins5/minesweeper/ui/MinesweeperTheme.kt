package com.tainanlins5.minesweeper.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.tainanlins5.minesweeper.data.AppSettings
import com.tainanlins5.minesweeper.data.AppearanceMode

@Immutable
data class RetroColors(
    val desktop: Color,
    val panel: Color,
    val panelLight: Color,
    val panelDark: Color,
    val well: Color,
    val text: Color,
    val mutedText: Color,
    val accent: Color,
    val onAccent: Color,
)

fun colorFromHue(hue: Float, dark: Boolean): Color {
    val hsv = floatArrayOf(hue.coerceIn(0f, 360f), if (dark) 0.64f else 0.72f, if (dark) 0.88f else 0.82f)
    return Color(AndroidColor.HSVToColor(hsv))
}

fun shouldUseDarkTheme(settings: AppSettings, systemDark: Boolean): Boolean = when (settings.appearanceMode) {
    AppearanceMode.SYSTEM -> systemDark
    AppearanceMode.LIGHT -> false
    AppearanceMode.DARK -> true
}

fun retroColors(settings: AppSettings, dark: Boolean): RetroColors {
    val accent = colorFromHue(settings.themeHue, dark)
    val onAccent = if (accent.luminance() > 0.48f) Color(0xFF151515) else Color.White
    return if (dark) {
        RetroColors(
            desktop = Color(0xFF15171C),
            panel = Color(0xFF3A3D45),
            panelLight = Color(0xFF686C76),
            panelDark = Color(0xFF16181D),
            well = Color(0xFF08090B),
            text = Color(0xFFF5F5F5),
            mutedText = Color(0xFFB8BBC4),
            accent = accent,
            onAccent = onAccent,
        )
    } else {
        RetroColors(
            desktop = Color(0xFFD8D8D8),
            panel = Color(0xFFC0C0C0),
            panelLight = Color.White,
            panelDark = Color(0xFF707070),
            well = Color.Black,
            text = Color(0xFF171717),
            mutedText = Color(0xFF555555),
            accent = accent,
            onAccent = onAccent,
        )
    }
}

@Composable
fun MinesweeperTheme(settings: AppSettings, content: @Composable (RetroColors, Boolean) -> Unit) {
    val dark = shouldUseDarkTheme(settings, isSystemInDarkTheme())
    val retro = retroColors(settings, dark)
    val scheme = if (dark) {
        darkColorScheme(
            primary = retro.accent,
            onPrimary = retro.onAccent,
            surface = retro.panel,
            onSurface = retro.text,
            background = retro.desktop,
            onBackground = retro.text,
        )
    } else {
        lightColorScheme(
            primary = retro.accent,
            onPrimary = retro.onAccent,
            surface = retro.panel,
            onSurface = retro.text,
            background = retro.desktop,
            onBackground = retro.text,
        )
    }
    MaterialTheme(colorScheme = scheme) { content(retro, dark) }
}

private fun Color.luminance(): Float {
    fun channel(value: Float): Float = if (value <= 0.03928f) value / 12.92f else Math.pow(((value + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()
    return 0.2126f * channel(red) + 0.7152f * channel(green) + 0.0722f * channel(blue)
}
