package com.borarpg.feature.activity.presentation.formatter

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutFormatterTest {
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
    fun clampsNegativeDuration() {
        assertEquals("00:00:00", formatActivityTime(-1000))
    }
}
