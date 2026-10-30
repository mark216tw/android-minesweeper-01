package com.tainanlins5.minesweeper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GameResultEntity::class, ActiveGameEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE active_game ADD COLUMN undoUsed INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE active_game_new (
                        id INTEGER NOT NULL,
                        width INTEGER NOT NULL,
                        height INTEGER NOT NULL,
                        mineCount INTEGER NOT NULL,
                        difficulty TEXT NOT NULL,
                        status TEXT NOT NULL,
                        minesGenerated INTEGER NOT NULL,
                        elapsedMillis INTEGER NOT NULL,
                        encodedCells TEXT NOT NULL,
                        undoCount INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO active_game_new (
                        id, width, height, mineCount, difficulty, status,
                        minesGenerated, elapsedMillis, encodedCells, undoCount
                    )
                    SELECT
                        id, width, height, mineCount, difficulty, status,
                        minesGenerated, elapsedMillis, encodedCells,
                        CASE WHEN undoUsed = 1 THEN 1 ELSE 0 END
                    FROM active_game
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE active_game")
                db.execSQL("ALTER TABLE active_game_new RENAME TO active_game")
            }
        }
    }
}
