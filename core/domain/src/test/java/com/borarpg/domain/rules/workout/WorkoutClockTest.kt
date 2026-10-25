package com.borarpg.domain.rules.workout

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutClockTest {
    @Test
    fun pauseExcludesTimeUntilResume() {
        val clock = WorkoutClock()
        clock.resume(0, 1000)
        clock.pause(11000)
        assertEquals(10000L, clock.elapsed(61000))
        clock.resume(clock.elapsed(61000), 61000)
        assertEquals(15000L, clock.elapsed(66000))
    }

    @Test
    fun restoredSessionKeepsRecordedDuration() {
        val clock = WorkoutClock()
        clock.resume(120000, 500)
        assertEquals(125000L, clock.elapsed(5500))
    }

    @Test
    fun clockDoesNotGoBackwards() {
        val clock = WorkoutClock()
        clock.resume(1000, 2000)
        assertEquals(1000L, clock.elapsed(1000))
    }
}
