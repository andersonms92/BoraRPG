package com.borarpg.domain

import kotlinx.coroutines.flow.Flow

enum class WorkoutMutation {
    APPLIED,
    NOT_FOUND,
    INVALID_STATE,
}

interface WorkoutRepository {
    val workouts: Flow<List<WorkoutSession>>

    fun observeRoute(id: String): Flow<List<RoutePoint>>

    suspend fun current(): WorkoutSession?

    suspend fun checkpoint(session: WorkoutSession, newPoints: List<RoutePoint>, startIndex: Int)

    suspend fun delete(id: String)

    suspend fun recoverInterruptedSessions()

    suspend fun saveResult(id: String): WorkoutMutation

    suspend fun discard(id: String): WorkoutMutation
}
