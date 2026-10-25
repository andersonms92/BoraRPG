package com.borarpg.domain.model.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileTest {
    @Test
    fun emptyProfileIsIncomplete() {
        assertFalse(PlayerProfile().complete)
    }

    @Test
    fun validMeasurementsAreTyped() {
        assertTrue(PlayerProfile("Ana", 65.5, 170.0, 30).complete)
    }

    @Test
    fun rejectsInvalidMeasurements() {
        for (weight in listOf(Double.NaN, Double.POSITIVE_INFINITY, 19.0, 401.0)) {
            assertTrue(runCatching { PlayerProfile("Ana", weight, 170.0, 30) }.isFailure)
        }
    }
}
