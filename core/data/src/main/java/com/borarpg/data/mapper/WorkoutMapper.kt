package com.borarpg.data.mapper

import com.borarpg.data.local.entity.WorkoutEntity
import com.borarpg.data.local.entity.WorkoutPointEntity
import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType

internal fun WorkoutEntity.toDomain(route: List<RoutePoint> = emptyList()) =
    WorkoutSession(
        id,
        WorkoutType.valueOf(type),
        startedAt,
        elapsedMillis,
        distanceMeters,
        weightKg,
        WorkoutStatus.valueOf(status.uppercase()),
        route,
    )

internal fun WorkoutPointEntity.toDomain() = RoutePoint(latitude, longitude, time, segment)

internal fun WorkoutSession.toEntity() =
    WorkoutEntity(
        id,
        mode.name,
        startedAt,
        elapsedMillis,
        distanceMeters,
        weightKg,
        status.name.lowercase(),
    )

internal fun RoutePoint.toEntity(workoutId: String, position: Int) =
    WorkoutPointEntity(workoutId, position, latitude, longitude, time, segment)
