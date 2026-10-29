package com.borarpg.domain.usecase.player

import com.borarpg.domain.model.player.PlayerProfile
import com.borarpg.domain.usecase.RecordingPlayerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavePlayerProfileUseCaseTest {
    private val profile = PlayerProfile("  Ana  ", 65.5, 170.0, 30)

    @Test
    fun trimsNameAndPreservesTypedMeasurementsWhenSaving() = runTest {
        val repository = RecordingPlayerRepository()
        SavePlayerProfileUseCase(repository)(profile)
        assertEquals(listOf(profile.copy(name = "Ana")), repository.profiles)
    }

    @Test
    fun incompleteProfileNeverReachesPersistence() = runTest {
        val repository = RecordingPlayerRepository()
        val save = SavePlayerProfileUseCase(repository)
        val incomplete =
            listOf(
                profile.copy(name = "   "),
                profile.copy(weightKg = null),
                profile.copy(heightCm = null),
                profile.copy(ageYears = null),
            )
        for (value in incomplete) {
            assertTrue(runCatching { save(value) }.exceptionOrNull() is IllegalArgumentException)
        }
        assertTrue(repository.profiles.isEmpty())
    }

    @Test
    fun invalidMeasurementsCannotBePassedToPersistence() = runTest {
        val repository = RecordingPlayerRepository()
        val save = SavePlayerProfileUseCase(repository)
        for (weight in listOf(Double.NaN, Double.POSITIVE_INFINITY, 19.0, 401.0)) {
            val error = runCatching { save(profile.copy(weightKg = weight)) }.exceptionOrNull()
            assertTrue(error is IllegalArgumentException)
        }
        val heightError = runCatching { save(profile.copy(heightCm = 79.0)) }.exceptionOrNull()
        val ageError = runCatching { save(profile.copy(ageYears = 12)) }.exceptionOrNull()
        assertTrue(heightError is IllegalArgumentException)
        assertTrue(ageError is IllegalArgumentException)
        assertTrue(repository.profiles.isEmpty())
    }
}
