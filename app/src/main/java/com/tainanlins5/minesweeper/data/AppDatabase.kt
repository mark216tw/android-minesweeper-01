package com.tainanlins5.minesweeper.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GameResultEntity::class, ActiveGameEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
