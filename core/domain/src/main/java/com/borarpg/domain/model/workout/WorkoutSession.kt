package com.borarpg.domain.model.workout

data class WorkoutSession(
    val id: String,
    val mode: WorkoutType,
    val startedAt: Long,
    val elapsedMillis: Long,
    val distanceMeters: Double,
    val weightKg: Double?,
    val status: WorkoutStatus,
    val route: List<RoutePoint>,
)
