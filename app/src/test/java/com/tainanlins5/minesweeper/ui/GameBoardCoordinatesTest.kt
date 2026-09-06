package com.tainanlins5.minesweeper.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameBoardCoordinatesTest {
    @Test
    fun mediumBoardMapsTapAfterFitScaling() {
        val scale = 320f / (28f * 16f)
        val index = cellIndexAt(
            tapX = (7.5f * 28f * scale),
            tapY = 40f + (11.5f * 28f * scale),
            viewportWidth = 320f,
            viewportHeight = 400f,
            cellSize = 28f,
            columns = 16,
            rows = 16,
            scale = scale,
            translationX = 0f,
            translationY = 0f,
        )

        assertEquals(11 * 16 + 7, index)
    }

    @Test
    fun expertBoardMapsTapAfterFitScaling() {
        val scale = 360f / (28f * 30f)
        val displayedHeight = 28f * 16f * scale
        val top = (500f - displayedHeight) / 2f
        val index = cellIndexAt(
            tapX = 22.5f * 28f * scale,
            tapY = top + 9.5f * 28f * scale,
            viewportWidth = 360f,
            viewportHeight = 500f,
            cellSize = 28f,
            columns = 30,
            rows = 16,
            scale = scale,
            translationX = 0f,
            translationY = 0f,
        )

        assertEquals(9 * 30 + 22, index)
    }

    @Test
    fun translatedBoardMapsTapToVisibleCell() {
        val index = cellIndexAt(
            tapX = 180f,
            tapY = 210f,
            viewportWidth = 300f,
            viewportHeight = 400f,
            cellSize = 28f,
            columns = 16,
            rows = 16,
            scale = 1f,
            translationX = 30f,
            translationY = -20f,
        )

        assertEquals(9 * 16 + 8, index)
    }

    @Test
    fun tapOutsideBoardReturnsNull() {
        assertNull(
            cellIndexAt(
                tapX = 4f,
                tapY = 4f,
                viewportWidth = 400f,
                viewportHeight = 400f,
                cellSize = 28f,
                columns = 9,
                rows = 9,
                scale = 1f,
                translationX = 0f,
                translationY = 0f,
            ),
        )
    }
}
