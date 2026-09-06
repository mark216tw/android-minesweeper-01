package com.tainanlins5.minesweeper

import android.app.Application
import androidx.room.Room
import com.tainanlins5.minesweeper.data.AppDatabase
import com.tainanlins5.minesweeper.data.GameRepository
import com.tainanlins5.minesweeper.data.SettingsRepository

class MinesweeperApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "minesweeper.db").build()
    }
    val gameRepository by lazy { GameRepository(database.gameDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
}
