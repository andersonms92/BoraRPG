package com.borarpg.domain

import org.junit.Assert.*
import org.junit.Test

class WorkoutTest {
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

    @Test
    fun distanceMatchesOneDegreeAtEquator() {
        assertEquals(
            111194.9,
            routeDistance(RoutePoint(0.0, 0.0, 0, 0), RoutePoint(0.0, 1.0, 1, 0)),
            1.0,
        )
    }

    @Test
    fun identicalPointsHaveNoDistance() {
        val p = RoutePoint(-23.55, -46.63, 0, 0)
        assertEquals(0.0, routeDistance(p, p), .0001)
    }

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
