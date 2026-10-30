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
import com.tainanlins5.minesweeper.data.RetroStyle

@Immutable
data class RetroColors(
    val style: RetroStyle,
    val desktop: Color,
    val panel: Color,
    val revealedCell: Color,
    val cellGrid: Color,
    val panelLight: Color,
    val panelDark: Color,
    val well: Color,
    val text: Color,
    val mutedText: Color,
    val accent: Color,
    val onAccent: Color,
    val numberColors: List<Color>,
    val flagColor: Color,
    val wrongFlagColor: Color,
    val explodedCell: Color,
    val mineColor: Color,
    val explodedMineColor: Color,
    val mineHighlight: Color,
    val explodedMineHighlight: Color,
    val displayText: Color,
)

private const val SKY_BLUE_HUE = 210f

private val LightNumberColors = listOf(
    Color.Transparent,
    Color(0xFF0000FF),
    Color(0xFF008000),
    Color(0xFFFF0000),
    Color(0xFF000080),
    Color(0xFF800000),
    Color(0xFF008080),
    Color(0xFF111111),
    Color(0xFF666666),
)

private val DarkNumberColors = listOf(
    Color.Transparent,
    Color(0xFF7CB8FF),
    Color(0xFF6EE7A0),
    Color(0xFFFF8A80),
    Color(0xFFB39DFF),
    Color(0xFFFFB86B),
    Color(0xFF5FE0D0),
    Color(0xFFE6E6E6),
    Color(0xFFB0B0B0),
)

private val MacLightNumberColors = List(9) { index ->
    if (index == 0) Color.Transparent else Color(0xFF000000)
}

private val MacDarkNumberColors = List(9) { index ->
    if (index == 0) Color.Transparent else Color(0xFFF2F2F2)
}

fun colorFromHue(hue: Float, dark: Boolean): Color {
    val hsv = floatArrayOf(hue.coerceIn(0f, 360f), if (dark) 0.64f else 0.72f, if (dark) 0.88f else 0.82f)
    return Color(AndroidColor.HSVToColor(hsv))
}

fun shouldUseDarkTheme(settings: AppSettings, systemDark: Boolean): Boolean = when (settings.appearanceMode) {
    AppearanceMode.SYSTEM -> systemDark
    AppearanceMode.LIGHT -> false
    AppearanceMode.DARK -> true
}

fun retroColors(settings: AppSettings, dark: Boolean): RetroColors = when (settings.retroStyle) {
    RetroStyle.WINDOWS -> windowsColors(dark)
    RetroStyle.MACINTOSH -> macintoshColors(dark)
}

private fun windowsColors(dark: Boolean): RetroColors {
    val accent = colorFromHue(SKY_BLUE_HUE, dark)
    val onAccent = if (accent.luminance() > 0.48f) Color(0xFF151515) else Color.White
    return if (dark) {
        val text = Color(0xFFF5F5F5)
        RetroColors(
            style = RetroStyle.WINDOWS,
            desktop = Color(0xFF15171C),
            panel = Color(0xFF3A3D45),
            revealedCell = Color(0xFF3A3D45),
            cellGrid = Color(0xFF16181D),
            panelLight = Color(0xFF686C76),
            panelDark = Color(0xFF16181D),
            well = Color(0xFF08090B),
            text = text,
            mutedText = Color(0xFFB8BBC4),
            accent = accent,
            onAccent = onAccent,
            numberColors = DarkNumberColors,
            flagColor = Color(0xFFFF2020),
            wrongFlagColor = Color.Red,
            explodedCell = Color(0xFFFF5555),
            mineColor = text,
            explodedMineColor = text,
            mineHighlight = Color.White,
            explodedMineHighlight = Color.White,
            displayText = Color(0xFFFF3333),
        )
    } else {
        val text = Color(0xFF171717)
        RetroColors(
            style = RetroStyle.WINDOWS,
            desktop = Color(0xFFD8D8D8),
            panel = Color(0xFFC0C0C0),
            revealedCell = Color(0xFFC0C0C0),
            cellGrid = Color(0xFF707070),
            panelLight = Color.White,
            panelDark = Color(0xFF707070),
            well = Color.Black,
            text = text,
            mutedText = Color(0xFF555555),
            accent = accent,
            onAccent = onAccent,
            numberColors = LightNumberColors,
            flagColor = Color(0xFFFF2020),
            wrongFlagColor = Color.Red,
            explodedCell = Color(0xFFFF5555),
            mineColor = text,
            explodedMineColor = text,
            mineHighlight = Color.White,
            explodedMineHighlight = Color.White,
            displayText = Color(0xFFFF3333),
        )
    }
}

private fun macintoshColors(dark: Boolean): RetroColors = if (dark) {
    RetroColors(
        style = RetroStyle.MACINTOSH,
        desktop = Color(0xFF1C1C1C),
        panel = Color(0xFF2B2B2B),
        revealedCell = Color(0xFF1F1F1F),
        cellGrid = Color(0xFF707070),
        panelLight = Color(0xFFE8E8E8),
        panelDark = Color.Black,
        well = Color.Black,
        text = Color(0xFFF2F2F2),
        mutedText = Color(0xFFB8B8B8),
        accent = Color.White,
        onAccent = Color.Black,
        numberColors = MacDarkNumberColors,
        flagColor = Color(0xFFF2F2F2),
        wrongFlagColor = Color(0xFF9A9A9A),
        explodedCell = Color(0xFFF2F2F2),
        mineColor = Color(0xFFF2F2F2),
        explodedMineColor = Color.Black,
        mineHighlight = Color.Black,
        explodedMineHighlight = Color.White,
        displayText = Color(0xFFF2F2F2),
    )
} else {
    RetroColors(
        style = RetroStyle.MACINTOSH,
        desktop = Color(0xFFA8A8A8),
        panel = Color.White,
        revealedCell = Color(0xFFE8E8E8),
        cellGrid = Color.Black,
        panelLight = Color.White,
        panelDark = Color.Black,
        well = Color.White,
        text = Color.Black,
        mutedText = Color(0xFF444444),
        accent = Color.Black,
        onAccent = Color.White,
        numberColors = MacLightNumberColors,
        flagColor = Color.Black,
        wrongFlagColor = Color(0xFF555555),
        explodedCell = Color.Black,
        mineColor = Color.Black,
        explodedMineColor = Color.White,
        mineHighlight = Color.White,
        explodedMineHighlight = Color.Black,
        displayText = Color.Black,
    )
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
