package com.borarpg.domain.model.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestPointTest {
    private val quest = QuestPoint("point", "Praça", -23.55, -46.63, QuestKind.EXPLORATION)

    @Test
    fun rejectsBlankOrOversizedTitles() {
        for (title in listOf("", "   ", "a".repeat(61))) {
            assertTrue(runCatching { quest.copy(title = title) }.isFailure)
        }
    }

    @Test
    fun rejectsInvalidCoordinates() {
        for (latitude in listOf(-91.0, 91.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertTrue(runCatching { quest.copy(latitude = latitude) }.isFailure)
        }
        for (longitude in listOf(-181.0, 181.0, Double.NaN, Double.NEGATIVE_INFINITY)) {
            assertTrue(runCatching { quest.copy(longitude = longitude) }.isFailure)
        }
    }

    @Test
    fun acceptsCoordinateAndTitleBoundaries() {
        assertEquals(
            60,
            quest.copy(title = "a".repeat(60), latitude = -90.0, longitude = 180.0).title.length,
        )
        assertEquals(90.0, quest.copy(latitude = 90.0, longitude = -180.0).latitude, 0.0)
    }
}
