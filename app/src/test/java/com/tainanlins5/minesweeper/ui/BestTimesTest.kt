package com.tainanlins5.minesweeper.ui

import com.tainanlins5.minesweeper.data.GameResult
import com.tainanlins5.minesweeper.domain.Difficulty
import com.tainanlins5.minesweeper.domain.ResultType
import org.junit.Assert.assertEquals
import org.junit.Test

class BestTimesTest {
    @Test
    fun bestTimesUseOnlyWinsAndKeepFastestTimePerDifficulty() {
        val results = listOf(
            result(ResultType.WON, Difficulty.BEGINNER, 12_000L),
            result(ResultType.WON, Difficulty.BEGINNER, 8_000L),
            result(ResultType.LOST, Difficulty.BEGINNER, 3_000L),
            result(ResultType.ABANDONED, Difficulty.INTERMEDIATE, 1_000L),
            result(ResultType.WON, Difficulty.INTERMEDIATE, 45_000L),
        )

        assertEquals(
            mapOf(
                Difficulty.BEGINNER to 8_000L,
                Difficulty.INTERMEDIATE to 45_000L,
            ),
            bestTimesByDifficulty(results),
        )
    }

    @Test
    fun difficultyWithoutWinsHasNoBestTime() {
        assertEquals(emptyMap<Difficulty, Long>(), bestTimesByDifficulty(emptyList()))
    }

    private fun result(resultType: ResultType, difficulty: Difficulty, elapsedMillis: Long) =
        GameResult(
            id = 0L,
            resultType = resultType,
            difficulty = difficulty,
            width = difficulty.width,
            height = difficulty.height,
            mineCount = difficulty.mines,
            elapsedMillis = elapsedMillis,
            endedAtMillis = 0L,
        )
}
