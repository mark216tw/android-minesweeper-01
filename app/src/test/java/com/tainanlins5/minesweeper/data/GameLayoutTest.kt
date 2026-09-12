package com.tainanlins5.minesweeper.data

import org.junit.Assert.assertEquals
import org.junit.Test

class GameLayoutTest {
    @Test
    fun missingLayoutDefaultsToClassic() {
        assertEquals(GameLayout.CLASSIC, decodeGameLayout(null))
    }

    @Test
    fun unknownLayoutDefaultsToClassic() {
        assertEquals(GameLayout.CLASSIC, decodeGameLayout("UNKNOWN"))
    }

    @Test
    fun savedHandheldLayoutIsRestored() {
        assertEquals(GameLayout.HANDHELD, decodeGameLayout("HANDHELD"))
    }
}
