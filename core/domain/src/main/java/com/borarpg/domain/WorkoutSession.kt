package com.borarpg.domain

enum class WorkoutStatus {
    ACTIVE,
    PAUSED,
    DRAFT,
    SAVED,
}

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
