package com.tainanlins5.minesweeper.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RetroStyleTest {
    @Test
    fun missingStyleDefaultsToWindows() {
        assertEquals(RetroStyle.WINDOWS, decodeRetroStyle(null))
    }

    @Test
    fun unknownStyleDefaultsToWindows() {
        assertEquals(RetroStyle.WINDOWS, decodeRetroStyle("UNKNOWN"))
    }

    @Test
    fun savedMacintoshStyleIsRestored() {
        assertEquals(RetroStyle.MACINTOSH, decodeRetroStyle("MACINTOSH"))
    }

    @Test
    fun savedNewStylesAreRestored() {
        assertEquals(RetroStyle.GAMEBOY, decodeRetroStyle("GAMEBOY"))
        assertEquals(RetroStyle.C64, decodeRetroStyle("C64"))
        assertEquals(RetroStyle.FAMICOM, decodeRetroStyle("FAMICOM"))
        assertEquals(RetroStyle.APPLE2, decodeRetroStyle("APPLE2"))
    }

    @Test
    fun savedWindowsGreenStyleIsRestored() {
        assertEquals(RetroStyle.WINGREEN, decodeRetroStyle("WINGREEN"))
    }

    @Test
    fun removedStylesFallBackToWindows() {
        assertEquals(RetroStyle.WINDOWS, decodeRetroStyle("WIN31"))
        assertEquals(RetroStyle.WINDOWS, decodeRetroStyle("WIN98"))
        assertEquals(RetroStyle.WINDOWS, decodeRetroStyle("WIN2K"))
        assertEquals(RetroStyle.WINDOWS, decodeRetroStyle("DOS"))
        assertEquals(RetroStyle.WINDOWS, decodeRetroStyle("ZXSPECTRUM"))
    }

    @Test
    fun styleLabelsAreUnique() {
        val labels = RetroStyle.entries.map { it.label }
        assertEquals(labels.size, labels.distinct().size)
    }

    @Test
    fun everyStyleIsDecodable() {
        RetroStyle.entries.forEach { style ->
            assertEquals(style, decodeRetroStyle(style.name))
        }
    }
}
