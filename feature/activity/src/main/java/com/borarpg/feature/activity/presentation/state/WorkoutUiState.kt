package com.borarpg.feature.activity.presentation.state

import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.TrackingState
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType
import com.borarpg.domain.rules.workout.resolveWorkoutWeight
import com.borarpg.domain.rules.workout.workoutCalories

data class WorkoutUiState(
    val loading: Boolean = true,
    val mode: WorkoutType = WorkoutType.WALK,
    val current: WorkoutSession? = null,
    val displayed: WorkoutSession? = null,
    val history: List<WorkoutSession> = emptyList(),
    val historyId: String? = null,
    val route: List<RoutePoint> = emptyList(),
    val tracking: TrackingState = TrackingState(),
    val busy: Boolean = false,
    val showHistory: Boolean = false,
    val showCancel: Boolean = false,
    val message: String? = null,
    val profileWeightKg: Double? = null,
) {
    val elapsedMillis: Long
        get() =
            if (current?.status == WorkoutStatus.ACTIVE && tracking.sessionId == current.id)
                maxOf(current.elapsedMillis, tracking.elapsedMillis)
            else displayed?.elapsedMillis ?: 0

    val distanceMeters: Double
        get() = displayed?.distanceMeters ?: 0.0

    val calories: Double?
        get() = displayed?.let {
            workoutCalories(it.mode, calorieWeightKg, elapsedMillis, distanceMeters)
        }

    val calorieWeightKg: Double?
        get() = resolveWorkoutWeight(displayed?.weightKg, profileWeightKg)

    val caloriesUseCurrentWeight: Boolean
        get() =
            displayed != null &&
                resolveWorkoutWeight(displayed.weightKg) == null &&
                calorieWeightKg != null

    val caloriesPerMeter: Double?
        get() = calories?.takeIf { distanceMeters > 0 }?.div(distanceMeters)

    val caloriesPerKilometer: Double?
        get() = caloriesPerMeter?.times(1000)
}
