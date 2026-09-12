package com.tainanlins5.minesweeper

import org.junit.Assert.assertEquals
import org.junit.Test

class GameClockTest {
    @Test
    fun elapsedAtAddsTimeSinceCheckpoint() {
        assertEquals(3_500L, elapsedAt(elapsedMillis = 1_500L, startedAt = 10_000L, now = 12_000L))
    }

    @Test
    fun elapsedAtDoesNotAdvanceWhenClockIsStopped() {
        assertEquals(1_500L, elapsedAt(elapsedMillis = 1_500L, startedAt = null, now = 12_000L))
    }

    @Test
    fun elapsedAtIgnoresAClockThatMovesBackwards() {
        assertEquals(1_500L, elapsedAt(elapsedMillis = 1_500L, startedAt = 12_000L, now = 10_000L))
    }
}
