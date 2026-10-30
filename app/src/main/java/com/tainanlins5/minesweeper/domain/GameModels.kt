package com.tainanlins5.minesweeper.domain

enum class Difficulty(
    val label: String,
    val width: Int,
    val height: Int,
    val mines: Int,
    val undoLimit: Int,
) {
    BEGINNER("初級", 9, 9, 10, 1),
    INTERMEDIATE("中級", 16, 16, 40, 2),
    EXPERT("高級", 30, 16, 99, 3),
    CUSTOM("自訂", 0, 0, 0, 3),
}

enum class GameStatus {
    READY,
    ACTIVE,
    WON,
    LOST,
}

enum class ResultType(val label: String) {
    WON("勝利"),
    LOST("失敗"),
    ABANDONED("放棄"),
}

data class Cell(
    val hasMine: Boolean = false,
    val adjacentMines: Int = 0,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val isExploded: Boolean = false,
)

data class GameState(
    val width: Int,
    val height: Int,
    val mineCount: Int,
    val difficulty: Difficulty,
    val status: GameStatus = GameStatus.READY,
    val cells: List<Cell> = List(width * height) { Cell() },
    val minesGenerated: Boolean = false,
    val elapsedMillis: Long = 0L,
    val undoCount: Int = 0,
) {
    val flagCount: Int get() = cells.count(Cell::isFlagged)
    val remainingMines: Int get() = mineCount - flagCount
    val hasStarted: Boolean get() = status != GameStatus.READY || minesGenerated
    val undoLimit: Int get() = difficulty.undoLimit
    val undosRemaining: Int get() = (undoLimit - undoCount).coerceAtLeast(0)
    val canUndo: Boolean get() = undoCount < undoLimit
}
