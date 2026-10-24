package com.borarpg.domain

import org.junit.Assert.*
import org.junit.Test

class ActivityTimeTest {
    @Test
    fun formatsSecondsMinutesAndHours() {
        assertEquals("00:00:00", formatActivityTime(999))
        assertEquals("00:01:05", formatActivityTime(65000))
        assertEquals("02:03:04", formatActivityTime(7384000))
    }

    @Test
    fun supportsActivitiesLongerThanOneDay() {
        assertEquals("25:00:00", formatActivityTime(90000000))
    }

    @Test
    fun elapsedTimeUsesClockDifferenceAndClampsNegativeValues() {
        assertEquals(65000L, activityElapsedMillis(100000, 165000))
        assertEquals(0L, activityElapsedMillis(100000, 99000))
        assertEquals("00:00:00", formatActivityTime(-1000))
    }
}
