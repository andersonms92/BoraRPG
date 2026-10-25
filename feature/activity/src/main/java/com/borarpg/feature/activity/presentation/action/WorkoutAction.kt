package com.borarpg.feature.activity.presentation.action

import com.borarpg.domain.model.workout.WorkoutType

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
