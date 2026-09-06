package com.tainanlins5.minesweeper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resultType: String,
    val difficulty: String,
    val width: Int,
    val height: Int,
    val mineCount: Int,
    val elapsedMillis: Long,
    val endedAtMillis: Long,
)

@Entity(tableName = "active_game")
data class ActiveGameEntity(
    @PrimaryKey val id: Int = ACTIVE_GAME_ID,
    val width: Int,
    val height: Int,
    val mineCount: Int,
    val difficulty: String,
    val status: String,
    val minesGenerated: Boolean,
    val elapsedMillis: Long,
    val encodedCells: String,
) {
    companion object {
        const val ACTIVE_GAME_ID = 1
    }
}
