package com.tainanlins5.minesweeper.domain

import kotlin.random.Random

object MinesweeperEngine {
    const val MIN_SIZE = 5
    const val MAX_WIDTH = 30
    const val MAX_HEIGHT = 24

    fun newGame(
        difficulty: Difficulty,
        width: Int = difficulty.width,
        height: Int = difficulty.height,
        mines: Int = difficulty.mines,
    ): GameState {
        require(width in MIN_SIZE..MAX_WIDTH) { "棋盤寬度必須介於 $MIN_SIZE 與 $MAX_WIDTH" }
        require(height in MIN_SIZE..MAX_HEIGHT) { "棋盤高度必須介於 $MIN_SIZE 與 $MAX_HEIGHT" }
        require(mines in 1..(width * height - 9)) { "地雷數量超出安全範圍" }
        return GameState(width, height, mines, difficulty)
    }

    fun reveal(state: GameState, index: Int, random: Random = Random.Default): GameState {
        if (state.status == GameStatus.WON || state.status == GameStatus.LOST) return state
        if (index !in state.cells.indices) return state
        if (state.cells[index].isFlagged || state.cells[index].isRevealed) return state

        val prepared = if (state.minesGenerated) state else generateMines(state, index, random)
        return revealPrepared(prepared, listOf(index))
    }

    fun chord(state: GameState, index: Int): GameState {
        if (state.status != GameStatus.ACTIVE || index !in state.cells.indices) return state
        val cell = state.cells[index]
        if (!cell.isRevealed || cell.adjacentMines == 0) return state

        val neighbors = neighbors(state.width, state.height, index)
        if (neighbors.count { state.cells[it].isFlagged } != cell.adjacentMines) return state
        val candidates = neighbors.filter { !state.cells[it].isFlagged && !state.cells[it].isRevealed }
        return revealPrepared(state, candidates)
    }

    fun toggleFlag(state: GameState, index: Int): GameState {
        if (state.status == GameStatus.WON || state.status == GameStatus.LOST) return state
        if (index !in state.cells.indices || state.cells[index].isRevealed) return state
        val cells = state.cells.toMutableList()
        cells[index] = cells[index].copy(isFlagged = !cells[index].isFlagged)
        return state.copy(cells = cells)
    }

    private fun generateMines(state: GameState, firstIndex: Int, random: Random): GameState {
        val excluded = neighbors(state.width, state.height, firstIndex).toMutableSet().apply {
            add(firstIndex)
        }
        val mineIndices = state.cells.indices
            .filterNot(excluded::contains)
            .shuffled(random)
            .take(state.mineCount)
            .toSet()

        val cells = List(state.cells.size) { index ->
            Cell(
                hasMine = index in mineIndices,
                adjacentMines = neighbors(state.width, state.height, index).count(mineIndices::contains),
                isFlagged = state.cells[index].isFlagged,
            )
        }
        return state.copy(cells = cells, minesGenerated = true, status = GameStatus.ACTIVE)
    }

    private fun revealPrepared(state: GameState, startingIndices: List<Int>): GameState {
        val cells = state.cells.toMutableList()
        val queue = ArrayDeque<Int>()
        startingIndices.forEach(queue::addLast)

        while (queue.isNotEmpty()) {
            val index = queue.removeFirst()
            val cell = cells[index]
            if (cell.isRevealed || cell.isFlagged) continue
            if (cell.hasMine) {
                cells[index] = cell.copy(isRevealed = true, isExploded = true)
                return state.copy(
                    cells = cells.map { if (it.hasMine) it.copy(isRevealed = true) else it },
                    status = GameStatus.LOST,
                )
            }

            cells[index] = cell.copy(isRevealed = true)
            if (cell.adjacentMines == 0) {
                neighbors(state.width, state.height, index).forEach { neighbor ->
                    val next = cells[neighbor]
                    if (!next.isRevealed && !next.isFlagged && !next.hasMine) queue.addLast(neighbor)
                }
            }
        }

        val won = cells.none { !it.hasMine && !it.isRevealed }
        return if (won) {
            state.copy(
                cells = cells.map { if (it.hasMine) it.copy(isFlagged = true) else it },
                status = GameStatus.WON,
            )
        } else {
            state.copy(cells = cells, status = GameStatus.ACTIVE)
        }
    }

    internal fun neighbors(width: Int, height: Int, index: Int): List<Int> {
        val x = index % width
        val y = index / width
        return buildList {
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    val nx = x + dx
                    val ny = y + dy
                    if (nx in 0 until width && ny in 0 until height) add(ny * width + nx)
                }
            }
        }
    }
}
