package com.tainanlins5.minesweeper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_results ORDER BY endedAtMillis DESC")
    fun observeResults(): Flow<List<GameResultEntity>>

    @Insert
    suspend fun insertResult(result: GameResultEntity)

    @Query("DELETE FROM game_results")
    suspend fun clearResults()

    @Query("SELECT * FROM active_game WHERE id = 1")
    suspend fun getActiveGame(): ActiveGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveGame(game: ActiveGameEntity)

    @Query("DELETE FROM active_game")
    suspend fun clearActiveGame()
}
