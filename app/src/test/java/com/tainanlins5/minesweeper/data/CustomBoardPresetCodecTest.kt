package com.tainanlins5.minesweeper.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomBoardPresetCodecTest {
    @Test
    fun presetsRoundTrip() {
        val presets = listOf(
            CustomBoardPreset(9, 9, 10),
            CustomBoardPreset(16, 16, 40),
            CustomBoardPreset(30, 24, 120),
        )

        assertEquals(presets, decodeCustomBoards(encodeCustomBoards(presets)))
    }

    @Test
    fun decodingDropsInvalidDuplicateAndExtraPresets() {
        val decoded = decodeCustomBoards("9,9,10|9,9,10|4,5,1|16,16,40|30,24,120|12,12,20")

        assertEquals(
            listOf(
                CustomBoardPreset(9, 9, 10),
                CustomBoardPreset(16, 16, 40),
                CustomBoardPreset(30, 24, 120),
            ),
            decoded,
        )
    }
}
