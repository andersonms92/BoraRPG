package com.borarpg.domain.rules.workout

import com.borarpg.domain.model.workout.WorkoutType

fun workoutCalories(type: WorkoutType, weight: Double?, elapsed: Long, meters: Double): Double? =
    resolveWorkoutWeight(weight)?.let {
        val met =
            when (type) {
                WorkoutType.WALK -> 3.8
                WorkoutType.RUN -> 8.5
            }
        if (meters <= 0) 0.0 else met * it * elapsed.coerceAtLeast(0) / 3600000.0
    }

fun resolveWorkoutWeight(recorded: Double?, current: Double? = null): Double? =
    listOf(recorded, current).firstOrNull { it != null && it.isFinite() && it in 20.0..400.0 }
