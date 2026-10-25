package com.borarpg.domain.rules.workout

import com.borarpg.domain.model.workout.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkoutCaloriesTest {
    @Test
    fun missingWeightDoesNotInventCalories() {
        assertNull(workoutCalories(WorkoutType.WALK, null, 3600000, 5000.0))
    }

    @Test
    fun walkingAndRunningUseDifferentIntensity() {
        assertEquals(266.0, workoutCalories(WorkoutType.WALK, 70.0, 3600000, 5000.0)!!, .001)
        assertEquals(595.0, workoutCalories(WorkoutType.RUN, 70.0, 3600000, 10000.0)!!, .001)
    }

    @Test
    fun caloriesScaleWithWeightAndDuration() {
        val base = workoutCalories(WorkoutType.WALK, 70.0, 1800000, 2500.0)!!
        assertEquals(base * 2, workoutCalories(WorkoutType.WALK, 140.0, 1800000, 2500.0)!!, .001)
    }

    @Test
    fun stationarySessionDoesNotClaimExerciseCalories() {
        assertEquals(0.0, workoutCalories(WorkoutType.RUN, 70.0, 3600000, 0.0)!!, 0.0)
    }
}
