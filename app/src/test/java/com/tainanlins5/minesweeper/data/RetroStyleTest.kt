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
}
