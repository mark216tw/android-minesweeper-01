package com.tainanlins5.minesweeper.data

import com.tainanlins5.minesweeper.domain.Cell
import com.tainanlins5.minesweeper.domain.Difficulty
import com.tainanlins5.minesweeper.domain.GameState
import com.tainanlins5.minesweeper.domain.GameStatus
import com.tainanlins5.minesweeper.domain.ResultType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class GameResult(
    val id: Long,
    val resultType: ResultType,
    val difficulty: Difficulty,
    val width: Int,
    val height: Int,
    val mineCount: Int,
    val elapsedMillis: Long,
    val endedAtMillis: Long,
)

class GameRepository(private val dao: GameDao) {
    fun observeResults(): Flow<List<GameResult>> = dao.observeResults().map { results ->
        results.map(GameResultEntity::toModel)
    }

    suspend fun loadActiveGame(): GameState? = dao.getActiveGame()?.toModel()

    suspend fun saveActiveGame(state: GameState) {
        dao.saveActiveGame(state.toEntity())
    }

    suspend fun clearActiveGame() = dao.clearActiveGame()

    suspend fun addResult(state: GameState, resultType: ResultType) {
        dao.insertResult(
            GameResultEntity(
                resultType = resultType.name,
                difficulty = state.difficulty.name,
                width = state.width,
                height = state.height,
                mineCount = state.mineCount,
                elapsedMillis = state.elapsedMillis,
                endedAtMillis = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun clearResults() = dao.clearResults()
}

private fun GameResultEntity.toModel() = GameResult(
    id = id,
    resultType = ResultType.valueOf(resultType),
    difficulty = Difficulty.valueOf(difficulty),
    width = width,
    height = height,
    mineCount = mineCount,
    elapsedMillis = elapsedMillis,
    endedAtMillis = endedAtMillis,
)

private fun GameState.toEntity() = ActiveGameEntity(
    width = width,
    height = height,
    mineCount = mineCount,
    difficulty = difficulty.name,
    status = status.name,
    minesGenerated = minesGenerated,
    elapsedMillis = elapsedMillis,
    encodedCells = buildString(cells.size * 2) {
        cells.forEach { cell ->
            val value =
                (if (cell.hasMine) 1 else 0) or
                    (if (cell.isRevealed) 2 else 0) or
                    (if (cell.isFlagged) 4 else 0) or
                    (if (cell.isExploded) 8 else 0) or
                    (cell.adjacentMines shl 4)
            append(value.toString(16).padStart(2, '0'))
        }
    },
)

private fun ActiveGameEntity.toModel(): GameState {
    val restoredCells = encodedCells.chunked(2).map { encoded ->
        val value = encoded.toInt(16)
        Cell(
            hasMine = value and 1 != 0,
            isRevealed = value and 2 != 0,
            isFlagged = value and 4 != 0,
            isExploded = value and 8 != 0,
            adjacentMines = value shr 4,
        )
    }
    if (restoredCells.size != width * height) return GameState(width, height, mineCount, Difficulty.valueOf(difficulty))
    return GameState(
        width = width,
        height = height,
        mineCount = mineCount,
        difficulty = Difficulty.valueOf(difficulty),
        status = GameStatus.valueOf(status),
        cells = restoredCells,
        minesGenerated = minesGenerated,
        elapsedMillis = elapsedMillis,
    )
}
