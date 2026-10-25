package com.borarpg.feature.activity.presentation.state

import com.borarpg.domain.model.workout.TrackingPhase
import com.borarpg.domain.model.workout.TrackingState
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutEnergyStateTest {
    private fun session(
        weight: Double? = 65.0,
        mode: WorkoutType = WorkoutType.WALK,
        elapsed: Long = 3_600_000,
        meters: Double = 5_000.0,
        status: WorkoutStatus = WorkoutStatus.SAVED,
    ) = WorkoutSession("session", mode, 1_000, elapsed, meters, weight, status, emptyList())

    @Test
    fun recordedWeightTakesPriorityOverTheCurrentProfile() {
        val result = session()
        val state = WorkoutUiState(displayed = result, profileWeightKg = 90.0)

        assertEquals(65.0, state.calorieWeightKg!!, .0001)
        assertEquals(247.0, state.calories!!, .0001)
        assertFalse(state.caloriesUseCurrentWeight)
        assertEquals(65.0, result.weightKg!!, .0001)
    }

    @Test
    fun olderSessionUsesProfileWeightWithoutChangingTheRecord() {
        val result = session(weight = null)
        val state = WorkoutUiState(displayed = result, profileWeightKg = 70.0)

        assertEquals(70.0, state.calorieWeightKg!!, .0001)
        assertEquals(266.0, state.calories!!, .0001)
        assertEquals(.0532, state.caloriesPerMeter!!, .0001)
        assertEquals(53.2, state.caloriesPerKilometer!!, .0001)
        assertTrue(state.caloriesUseCurrentWeight)
        assertNull(result.weightKg)
    }

    @Test
    fun profileChangesRecalculateOnlySessionsWithoutRecordedWeight() {
        val fallback = WorkoutUiState(displayed = session(weight = null), profileWeightKg = 70.0)
        val recorded = WorkoutUiState(displayed = session(), profileWeightKg = 70.0)

        assertEquals(304.0, fallback.copy(profileWeightKg = 80.0).calories!!, .0001)
        assertEquals(recorded.calories, recorded.copy(profileWeightKg = 80.0).calories)
    }

    @Test
    fun missingWeightDoesNotInventAnEstimate() {
        val state = WorkoutUiState(displayed = session(weight = null))

        assertNull(state.calorieWeightKg)
        assertNull(state.calories)
        assertNull(state.caloriesPerMeter)
        assertNull(state.caloriesPerKilometer)
        assertFalse(state.caloriesUseCurrentWeight)
    }

    @Test
    fun invalidProfileWeightsCannotProduceCalories() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, 19.9, 400.1).forEach { weight ->
            val state = WorkoutUiState(displayed = session(weight = null), profileWeightKg = weight)

            assertNull(state.calorieWeightKg)
            assertNull(state.calories)
            assertFalse(state.caloriesUseCurrentWeight)
        }
    }

    @Test
    fun walkingAndRunningKeepTheirOwnIntensity() {
        val walking = WorkoutUiState(displayed = session(weight = 70.0))
        val running = walking.copy(displayed = session(weight = 70.0, mode = WorkoutType.RUN))

        assertEquals(266.0, walking.calories!!, .0001)
        assertEquals(595.0, running.calories!!, .0001)
    }

    @Test
    fun stationarySessionDoesNotProduceCaloriesOrDistanceAverages() {
        val state = WorkoutUiState(displayed = session(meters = 0.0))

        assertEquals(0.0, state.calories!!, .0)
        assertNull(state.caloriesPerMeter)
        assertNull(state.caloriesPerKilometer)
    }

    @Test
    fun selectedSavedSessionUsesItsDurationAndMode() {
        val walking = session(weight = 70.0, elapsed = 1_800_000)
        val running = walking.copy(id = "run", mode = WorkoutType.RUN, elapsedMillis = 900_000)
        val state =
            WorkoutUiState(
                displayed = running,
                history = listOf(walking, running),
                historyId = running.id,
                profileWeightKg = 90.0,
            )

        assertEquals(900_000L, state.elapsedMillis)
        assertEquals(148.75, state.calories!!, .0001)
        assertFalse(state.caloriesUseCurrentWeight)
    }

    @Test
    fun liveElapsedTimeChangesCaloriesOnlyForTheMatchingSession() {
        val activity = session(weight = 70.0, elapsed = 1_800_000, status = WorkoutStatus.ACTIVE)
        val state =
            WorkoutUiState(
                current = activity,
                displayed = activity,
                tracking = TrackingState(TrackingPhase.RUNNING, "other", elapsedMillis = 3_600_000),
            )

        assertEquals(133.0, state.calories!!, .0001)
        val matching = state.copy(tracking = state.tracking.copy(sessionId = activity.id))
        assertEquals(266.0, matching.calories!!, .0001)
    }
}
