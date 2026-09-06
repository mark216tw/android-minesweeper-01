package com.tainanlins5.minesweeper.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MinesweeperEngineTest {
    @Test
    fun firstRevealProtectsEntireNeighborhood() {
        val initial = MinesweeperEngine.newGame(Difficulty.EXPERT)
        val center = 8 * initial.width + 15

        val state = MinesweeperEngine.reveal(initial, center, Random(7))

        val safeIndices = MinesweeperEngine.neighbors(state.width, state.height, center) + center
        assertTrue(safeIndices.none { state.cells[it].hasMine })
        assertEquals(99, state.cells.count { it.hasMine })
        assertTrue(state.cells[center].isRevealed)
    }

    @Test
    fun flagPreventsReveal() {
        val initial = MinesweeperEngine.newGame(Difficulty.BEGINNER)
        val flagged = MinesweeperEngine.toggleFlag(initial, 0)

        val state = MinesweeperEngine.reveal(flagged, 0, Random(3))

        assertTrue(state.cells[0].isFlagged)
        assertFalse(state.cells[0].isRevealed)
        assertEquals(GameStatus.READY, state.status)
    }

    @Test
    fun revealingMineLosesGame() {
        val active = MinesweeperEngine.reveal(
            MinesweeperEngine.newGame(Difficulty.BEGINNER),
            0,
            Random(12),
        )
        val mine = active.cells.indexOfFirst { it.hasMine }

        val state = MinesweeperEngine.reveal(active, mine)

        assertEquals(GameStatus.LOST, state.status)
        assertTrue(state.cells[mine].isExploded)
        assertTrue(state.cells.filter { it.hasMine }.all { it.isRevealed })
    }

    @Test
    fun validCustomGameUsesRequestedDimensions() {
        val state = MinesweeperEngine.newGame(Difficulty.CUSTOM, 30, 24, 200)

        assertEquals(720, state.cells.size)
        assertEquals(200, state.mineCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun customGameRejectsTooManyMines() {
        MinesweeperEngine.newGame(Difficulty.CUSTOM, 5, 5, 17)
    }

    @Test
    fun togglingFlagTwiceRestoresCell() {
        val initial = MinesweeperEngine.newGame(Difficulty.BEGINNER)
        val flagged = MinesweeperEngine.toggleFlag(initial, 5)
        val restored = MinesweeperEngine.toggleFlag(flagged, 5)

        assertNotEquals(initial.cells, flagged.cells)
        assertEquals(initial.cells, restored.cells)
    }

    @Test
    fun chordWithCorrectFlagRevealsNeighbors() {
        val cells = boardWithSingleMine().toMutableList()
        cells[0] = cells[0].copy(isFlagged = true)
        cells[6] = cells[6].copy(isRevealed = true)
        val active = GameState(5, 5, 1, Difficulty.CUSTOM, GameStatus.ACTIVE, cells, true)

        val state = MinesweeperEngine.chord(active, 6)

        assertEquals(GameStatus.WON, state.status)
        assertTrue(state.cells.filterNot { it.hasMine }.all { it.isRevealed })
    }

    @Test
    fun chordWithIncorrectFlagCanExplodeMine() {
        val cells = boardWithSingleMine().toMutableList()
        cells[1] = cells[1].copy(isFlagged = true)
        cells[6] = cells[6].copy(isRevealed = true)
        val active = GameState(5, 5, 1, Difficulty.CUSTOM, GameStatus.ACTIVE, cells, true)

        val state = MinesweeperEngine.chord(active, 6)

        assertEquals(GameStatus.LOST, state.status)
        assertTrue(state.cells[0].isExploded)
    }

    private fun boardWithSingleMine(): List<Cell> = List(25) { index ->
        Cell(
            hasMine = index == 0,
            adjacentMines = MinesweeperEngine.neighbors(5, 5, index).count { it == 0 },
        )
    }
}
