package com.tainanlins5.minesweeper.ui

import androidx.compose.ui.graphics.Color
import com.tainanlins5.minesweeper.data.AppSettings
import com.tainanlins5.minesweeper.data.RetroStyle
import org.junit.Assert.assertTrue
import org.junit.Test

class RetroPaletteContrastTest {
    private data class Sample(val style: RetroStyle, val dark: Boolean, val colors: RetroColors)

    private fun samples(): List<Sample> = RetroStyle.entries.flatMap { style ->
        listOf(false, true).map { dark ->
            Sample(style, dark, retroColors(AppSettings(retroStyle = style), dark))
        }
    }

    private fun channel(value: Double): Double =
        if (value <= 0.03928) value / 12.92 else Math.pow((value + 0.055) / 1.055, 2.4)

    private fun luminance(color: Color): Double = (
        0.2126 * channel(color.red.toDouble()) +
            0.7152 * channel(color.green.toDouble()) +
            0.0722 * channel(color.blue.toDouble())
        )

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        val hi = maxOf(la, lb) + 0.05
        val lo = minOf(la, lb) + 0.05
        return hi / lo
    }

    private fun Sample.assertAtLeast(foreground: Color, background: Color, threshold: Double, role: String) {
        val ratio = contrast(foreground, background)
        assertTrue(
            "$style ${if (dark) "dark" else "light"} $role contrast $ratio < $threshold",
            ratio >= threshold,
        )
    }

    @Test
    fun primaryTextIsReadable() {
        samples().forEach { it.assertAtLeast(it.colors.text, it.colors.panel, 4.5, "text/panel") }
    }

    @Test
    fun desktopTextIsReadable() {
        samples().forEach { it.assertAtLeast(it.colors.desktopText, it.colors.desktop, 4.5, "desktopText/desktop") }
    }

    @Test
    fun mutedTextIsReadable() {
        samples().forEach {
            it.assertAtLeast(it.colors.mutedText, it.colors.panel, 3.0, "mutedText/panel")
            it.assertAtLeast(it.colors.desktopMutedText, it.colors.desktop, 3.0, "desktopMutedText/desktop")
        }
    }

    @Test
    fun selectedColorsAreReadable() {
        samples().forEach { it.assertAtLeast(it.colors.onAccent, it.colors.accent, 3.0, "onAccent/accent") }
    }

    @Test
    fun displayTextIsReadable() {
        samples().forEach { it.assertAtLeast(it.colors.displayText, it.colors.well, 3.0, "displayText/well") }
    }

    @Test
    fun numberColorsAreReadable() {
        samples()
            .filter { it.style != RetroStyle.WINDOWS }
            .forEach { sample ->
                sample.colors.numberColors.drop(1).forEachIndexed { index, color ->
                    sample.assertAtLeast(color, sample.colors.revealedCell, 2.5, "number${index + 1}/revealedCell")
                }
            }
    }
}
