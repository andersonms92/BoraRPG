package com.borarpg.domain.usecase.quest

import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.usecase.RecordingPlayerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateQuestUseCaseTest {
    @Test
    fun createsTrimmedExplorationLocationsWithAnIdForEachRequest() = runTest {
        val repository = RecordingPlayerRepository()
        var sequence = 0
        val create = CreateQuestUseCase(repository) { "place:${++sequence}" }
        create("  Praça central  ", -23.55, -46.63)
        create("Parque", -23.56, -46.64)
        assertEquals(listOf("place:1", "place:2"), repository.savedQuests.map { it.id })
        assertEquals("Praça central", repository.savedQuests.first().title)
        assertTrue(repository.savedQuests.all { it.kind == QuestKind.EXPLORATION })
        assertFalse(repository.savedQuests.first().generated)
        assertEquals(-23.55, repository.savedQuests.first().latitude, 0.0)
        assertEquals(-46.63, repository.savedQuests.first().longitude, 0.0)
    }

    @Test
    fun rejectsInvalidTitlesBeforePersistingAnyLocation() = runTest {
        val repository = RecordingPlayerRepository()
        val create = CreateQuestUseCase(repository) { "place" }
        for (title in listOf("", "   ", "a".repeat(61))) {
            val error = runCatching { create(title, -23.55, -46.63) }.exceptionOrNull()
            assertTrue(error is IllegalArgumentException)
        }
        assertTrue(repository.savedQuests.isEmpty())
    }

    @Test
    fun rejectsInvalidCoordinatesBeforePersistingAnyLocation() = runTest {
        val repository = RecordingPlayerRepository()
        val create = CreateQuestUseCase(repository) { "place" }
        for ((latitude, longitude) in listOf(91.0 to 0.0, 0.0 to -181.0, Double.NaN to 0.0)) {
            val error = runCatching { create("Praça", latitude, longitude) }.exceptionOrNull()
            assertTrue(error is IllegalArgumentException)
        }
        assertTrue(repository.savedQuests.isEmpty())
    }
}
