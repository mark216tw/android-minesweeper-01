package com.tainanlins5.minesweeper

import com.tainanlins5.minesweeper.domain.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDifficultyTest {
    @Test
    fun defaultDifficultyIsIntermediate() {
        assertEquals(Difficulty.INTERMEDIATE, DEFAULT_DIFFICULTY)
    }
}
