package com.borarpg.feature.activity

import com.borarpg.domain.*

sealed interface WorkoutAction {
    data class SelectMode(val mode: WorkoutType) : WorkoutAction

    data class SelectHistory(val id: String) : WorkoutAction

    data object Follow : WorkoutAction

    data object Start : WorkoutAction

    data object Pause : WorkoutAction

    data object Resume : WorkoutAction

    data object Finish : WorkoutAction

    data object Cancel : WorkoutAction

    data object Save : WorkoutAction

    data object Discard : WorkoutAction

    data object ShowHistory : WorkoutAction

    data object HideHistory : WorkoutAction

    data object ShowCancel : WorkoutAction

    data object HideCancel : WorkoutAction

    data object Retry : WorkoutAction
}

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
            workoutCalories(it.mode, it.weightKg, elapsedMillis, distanceMeters)
        }
}
