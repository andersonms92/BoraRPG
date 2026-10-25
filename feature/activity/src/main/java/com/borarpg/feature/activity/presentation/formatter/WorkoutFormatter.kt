package com.borarpg.feature.activity.presentation.formatter

import com.borarpg.domain.model.workout.WorkoutType

fun formatActivityTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    return listOf(seconds / 3600, seconds / 60 % 60, seconds % 60).joinToString(":") {
        it.toString().padStart(2, '0')
    }
}

internal val WorkoutType.label: String
    get() =
        when (this) {
            WorkoutType.WALK -> "Caminhada"
            WorkoutType.RUN -> "Corrida"
        }
