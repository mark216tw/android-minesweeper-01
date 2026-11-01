package com.tainanlins5.minesweeper.ui

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
    val desktopText: Color,
    val desktopMutedText: Color,
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

private val TunedLightNumberColors = numbers(0xFF0000C0, 0xFF006400, 0xFFD00000, 0xFF000080, 0xFF800000, 0xFF006060, 0xFF101010, 0xFF606060)

private val TunedDarkNumberColors = numbers(0xFF7C9CFF, 0xFF3FBF5F, 0xFFFF6060, 0xFF9A9AFF, 0xFFE07070, 0xFF40C0C0, 0xFFF0F0F0, 0xFFB0B0B0)

fun colorFromHue(hue: Float, dark: Boolean): Color {
    val h = hue.coerceIn(0f, 360f) / 60f
    val saturation = if (dark) 0.64f else 0.72f
    val value = if (dark) 0.88f else 0.82f
    val chroma = value * saturation
    val secondary = chroma * (1f - kotlin.math.abs(h % 2f - 1f))
    val min = value - chroma
    val (red, green, blue) = when (h.toInt()) {
        0 -> Triple(chroma, secondary, 0f)
        1 -> Triple(secondary, chroma, 0f)
        2 -> Triple(0f, chroma, secondary)
        3 -> Triple(0f, secondary, chroma)
        4 -> Triple(secondary, 0f, chroma)
        else -> Triple(chroma, 0f, secondary)
    }
    return Color(red + min, green + min, blue + min)
}

fun shouldUseDarkTheme(settings: AppSettings, systemDark: Boolean): Boolean = when (settings.appearanceMode) {
    AppearanceMode.SYSTEM -> systemDark
    AppearanceMode.LIGHT -> false
    AppearanceMode.DARK -> true
}

fun retroColors(settings: AppSettings, dark: Boolean): RetroColors = when (settings.retroStyle) {
    RetroStyle.WINDOWS -> windowsColors(dark)
    RetroStyle.WINGREEN -> winGreenColors(dark)
    RetroStyle.MACINTOSH -> macintoshColors(dark)
    RetroStyle.GAMEBOY -> gameBoyColors(dark)
    RetroStyle.C64 -> commodoreColors(dark)
    RetroStyle.FAMICOM -> famicomColors(dark)
    RetroStyle.APPLE2 -> appleColors(dark)
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
            desktopText = text,
            desktopMutedText = Color(0xFFB8BBC4),
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
            desktopText = text,
            desktopMutedText = Color(0xFF555555),
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
        desktopText = Color(0xFFF2F2F2),
        desktopMutedText = Color(0xFFB8B8B8),
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
        desktopText = Color.Black,
        desktopMutedText = Color(0xFF444444),
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

private fun numbers(vararg colors: Long): List<Color> =
    listOf(Color.Transparent) + colors.map { Color(it) }

private fun twoToneNumbers(odd: Long, even: Long): List<Color> =
    listOf(Color.Transparent) + List(8) { index -> Color(if (index % 2 == 0) odd else even) }

private fun retroPalette(
    style: RetroStyle,
    desktop: Long,
    panel: Long,
    revealedCell: Long,
    cellGrid: Long,
    panelLight: Long,
    panelDark: Long,
    well: Long,
    text: Long,
    mutedText: Long,
    desktopText: Long = text,
    desktopMutedText: Long = mutedText,
    accent: Long,
    onAccent: Long,
    numberColors: List<Color>,
    flagColor: Long,
    wrongFlagColor: Long,
    explodedCell: Long,
    mineColor: Long,
    explodedMineColor: Long,
    mineHighlight: Long,
    explodedMineHighlight: Long,
    displayText: Long,
): RetroColors = RetroColors(
    style = style,
    desktop = Color(desktop),
    panel = Color(panel),
    revealedCell = Color(revealedCell),
    cellGrid = Color(cellGrid),
    panelLight = Color(panelLight),
    panelDark = Color(panelDark),
    well = Color(well),
    text = Color(text),
    mutedText = Color(mutedText),
    desktopText = Color(desktopText),
    desktopMutedText = Color(desktopMutedText),
    accent = Color(accent),
    onAccent = Color(onAccent),
    numberColors = numberColors,
    flagColor = Color(flagColor),
    wrongFlagColor = Color(wrongFlagColor),
    explodedCell = Color(explodedCell),
    mineColor = Color(mineColor),
    explodedMineColor = Color(explodedMineColor),
    mineHighlight = Color(mineHighlight),
    explodedMineHighlight = Color(explodedMineHighlight),
    displayText = Color(displayText),
)

private fun gameBoyColors(dark: Boolean): RetroColors = if (dark) {
    retroPalette(
        style = RetroStyle.GAMEBOY,
        desktop = 0xFF0E1B0C,
        panel = 0xFF16290F,
        revealedCell = 0xFF1D3A14,
        cellGrid = 0xFF050D04,
        panelLight = 0xFF4E7A2E,
        panelDark = 0xFF050D04,
        well = 0xFF050D04,
        text = 0xFFB7E36B,
        mutedText = 0xFF7FA64E,
        accent = 0xFF2A4A18,
        onAccent = 0xFFB7E36B,
        numberColors = twoToneNumbers(odd = 0xFFB7E36B, even = 0xFF7FA64E),
        flagColor = 0xFFCDEF8A,
        wrongFlagColor = 0xFFB08A2E,
        explodedCell = 0xFF3A5A18,
        mineColor = 0xFFB7E36B,
        explodedMineColor = 0xFF050D04,
        mineHighlight = 0xFF050D04,
        explodedMineHighlight = 0xFF7FA64E,
        displayText = 0xFFA5D66A,
    )
} else {
    retroPalette(
        style = RetroStyle.GAMEBOY,
        desktop = 0xFF9BBC0F,
        panel = 0xFF9BBC0F,
        revealedCell = 0xFF8BAC0F,
        cellGrid = 0xFF306230,
        panelLight = 0xFFD8E8A3,
        panelDark = 0xFF306230,
        well = 0xFF0F380F,
        text = 0xFF0F380F,
        mutedText = 0xFF306230,
        accent = 0xFF306230,
        onAccent = 0xFFD8E8A3,
        numberColors = twoToneNumbers(odd = 0xFF0F380F, even = 0xFF306230),
        flagColor = 0xFF0F380F,
        wrongFlagColor = 0xFF7A5A00,
        explodedCell = 0xFF306230,
        mineColor = 0xFF0F380F,
        explodedMineColor = 0xFF9BBC0F,
        mineHighlight = 0xFFFFFFFF,
        explodedMineHighlight = 0xFF306230,
        displayText = 0xFF9BBC0F,
    )
}

private fun commodoreColors(dark: Boolean): RetroColors = if (dark) {
    retroPalette(
        style = RetroStyle.C64,
        desktop = 0xFF241B4E,
        panel = 0xFF241B4E,
        revealedCell = 0xFF1D1542,
        cellGrid = 0xFF4A3E96,
        panelLight = 0xFF4A3E96,
        panelDark = 0xFF120C28,
        well = 0xFF120C28,
        text = 0xFFB1A7E3,
        mutedText = 0xFF7B71B2,
        accent = 0xFFB8C76F,
        onAccent = 0xFF1D1542,
        numberColors = numbers(0xFF7B71B2, 0xFF9AD284, 0xFF9A6759, 0xFFB1A7E3, 0xFFB8C76F, 0xFF70A4B2, 0xFFE8E8E8, 0xFF959595),
        flagColor = 0xFFB8C76F,
        wrongFlagColor = 0xFFE8E8E8,
        explodedCell = 0xFF6F3D86,
        mineColor = 0xFFB1A7E3,
        explodedMineColor = 0xFF120C28,
        mineHighlight = 0xFFFFFFFF,
        explodedMineHighlight = 0xFFB8C76F,
        displayText = 0xFFB8C76F,
    )
} else {
    retroPalette(
        style = RetroStyle.C64,
        desktop = 0xFF352879,
        panel = 0xFF352879,
        revealedCell = 0xFF2B2062,
        cellGrid = 0xFF6C5EB5,
        panelLight = 0xFF7B71B2,
        panelDark = 0xFF1D1542,
        well = 0xFF1D1542,
        text = 0xFFB1A7E3,
        mutedText = 0xFF959595,
        accent = 0xFFB8C76F,
        onAccent = 0xFF1D1542,
        numberColors = numbers(0xFF9E95E8, 0xFF9AD284, 0xFFB98A7C, 0xFFB1A7E3, 0xFFB8C76F, 0xFF70A4B2, 0xFFFFFFFF, 0xFF959595),
        flagColor = 0xFFB8C76F,
        wrongFlagColor = 0xFFFFFFFF,
        explodedCell = 0xFF9A6759,
        mineColor = 0xFFB1A7E3,
        explodedMineColor = 0xFF1D1542,
        mineHighlight = 0xFFFFFFFF,
        explodedMineHighlight = 0xFFB8C76F,
        displayText = 0xFFB8C76F,
    )
}


private fun famicomColors(dark: Boolean): RetroColors = if (dark) {
    retroPalette(
        style = RetroStyle.FAMICOM,
        desktop = 0xFF241E14,
        panel = 0xFF332B1E,
        revealedCell = 0xFF2A2418,
        cellGrid = 0xFF8A7E5C,
        panelLight = 0xFF57503C,
        panelDark = 0xFF12100A,
        well = 0xFF000000,
        text = 0xFFEDE4C5,
        mutedText = 0xFFA89870,
        accent = 0xFFF83800,
        onAccent = 0xFFFCFCFC,
        numberColors = numbers(0xFF3CBCFC, 0xFF58D854, 0xFFF83800, 0xFF6C8CF8, 0xFFF87858, 0xFF3CE0D0, 0xFFFCFCFC, 0xFFBCBCBC),
        flagColor = 0xFFF83800,
        wrongFlagColor = 0xFFFCFCFC,
        explodedCell = 0xFFF83800,
        mineColor = 0xFFEDE4C5,
        explodedMineColor = 0xFF000000,
        mineHighlight = 0xFF241E14,
        explodedMineHighlight = 0xFFFCFCFC,
        displayText = 0xFFFCFCFC,
    )
} else {
    retroPalette(
        style = RetroStyle.FAMICOM,
        desktop = 0xFFEDE4C5,
        panel = 0xFFFCFCFC,
        revealedCell = 0xFFDCD6C4,
        cellGrid = 0xFF101010,
        panelLight = 0xFFFFFFFF,
        panelDark = 0xFFA8A4A0,
        well = 0xFF000000,
        text = 0xFF101010,
        mutedText = 0xFF7B7054,
        accent = 0xFFD82800,
        onAccent = 0xFFFCFCFC,
        numberColors = numbers(0xFF0058F8, 0xFF008400, 0xFFD82800, 0xFF0038A8, 0xFF881400, 0xFF007878, 0xFF101010, 0xFF6C6C6C),
        flagColor = 0xFFD82800,
        wrongFlagColor = 0xFF101010,
        explodedCell = 0xFFD82800,
        mineColor = 0xFF101010,
        explodedMineColor = 0xFF000000,
        mineHighlight = 0xFFFCFCFC,
        explodedMineHighlight = 0xFFFCFCFC,
        displayText = 0xFFFCFCFC,
    )
}


private fun appleColors(dark: Boolean): RetroColors = if (dark) {
    retroPalette(
        style = RetroStyle.APPLE2,
        desktop = 0xFF030B03,
        panel = 0xFF030B03,
        revealedCell = 0xFF081C08,
        cellGrid = 0xFF1E7A2E,
        panelLight = 0xFF35C24A,
        panelDark = 0xFF010501,
        well = 0xFF010501,
        text = 0xFF35C24A,
        mutedText = 0xFF1E7A2E,
        accent = 0xFF35C24A,
        onAccent = 0xFF030B03,
        numberColors = twoToneNumbers(odd = 0xFF35C24A, even = 0xFF1E7A2E),
        flagColor = 0xFF35C24A,
        wrongFlagColor = 0xFF1E7A2E,
        explodedCell = 0xFF1E7A2E,
        mineColor = 0xFF35C24A,
        explodedMineColor = 0xFF030B03,
        mineHighlight = 0xFF030B03,
        explodedMineHighlight = 0xFF35C24A,
        displayText = 0xFF2E9E3F,
    )
} else {
    retroPalette(
        style = RetroStyle.APPLE2,
        desktop = 0xFF0A220A,
        panel = 0xFF0A220A,
        revealedCell = 0xFF0F330F,
        cellGrid = 0xFF2E9E3F,
        panelLight = 0xFF4CFF5E,
        panelDark = 0xFF031403,
        well = 0xFF031403,
        text = 0xFF4CFF5E,
        mutedText = 0xFF2E9E3F,
        accent = 0xFF4CFF5E,
        onAccent = 0xFF0A220A,
        numberColors = twoToneNumbers(odd = 0xFF4CFF5E, even = 0xFF2E9E3F),
        flagColor = 0xFF4CFF5E,
        wrongFlagColor = 0xFF2E9E3F,
        explodedCell = 0xFF2E9E3F,
        mineColor = 0xFF4CFF5E,
        explodedMineColor = 0xFF0A220A,
        mineHighlight = 0xFF0A220A,
        explodedMineHighlight = 0xFF4CFF5E,
        displayText = 0xFF4CFF5E,
    )
}

private fun winGreenColors(dark: Boolean): RetroColors = if (dark) {
    retroPalette(
        style = RetroStyle.WINGREEN,
        desktop = 0xFF15171C,
        panel = 0xFF3A3D45,
        revealedCell = 0xFF3A3D45,
        cellGrid = 0xFF16181D,
        panelLight = 0xFF686C76,
        panelDark = 0xFF16181D,
        well = 0xFF08090B,
        text = 0xFFF5F5F5,
        mutedText = 0xFFB8BBC4,
        accent = 0xFF2E8C64,
        onAccent = 0xFFFFFFFF,
        numberColors = TunedDarkNumberColors,
        flagColor = 0xFFFF2020,
        wrongFlagColor = 0xFFFF0000,
        explodedCell = 0xFFFF5555,
        mineColor = 0xFFF5F5F5,
        explodedMineColor = 0xFFF5F5F5,
        mineHighlight = 0xFFFFFFFF,
        explodedMineHighlight = 0xFFFFFFFF,
        displayText = 0xFFFF3333,
    )
} else {
    retroPalette(
        style = RetroStyle.WINGREEN,
        desktop = 0xFFD8D8D8,
        panel = 0xFFC0C0C0,
        revealedCell = 0xFFC0C0C0,
        cellGrid = 0xFF707070,
        panelLight = 0xFFFFFFFF,
        panelDark = 0xFF707070,
        well = 0xFF000000,
        text = 0xFF171717,
        mutedText = 0xFF555555,
        accent = 0xFF0F7A52,
        onAccent = 0xFFFFFFFF,
        numberColors = TunedLightNumberColors,
        flagColor = 0xFFFF2020,
        wrongFlagColor = 0xFFFF0000,
        explodedCell = 0xFFFF5555,
        mineColor = 0xFF171717,
        explodedMineColor = 0xFF171717,
        mineHighlight = 0xFFFFFFFF,
        explodedMineHighlight = 0xFFFFFFFF,
        displayText = 0xFFFF3333,
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
