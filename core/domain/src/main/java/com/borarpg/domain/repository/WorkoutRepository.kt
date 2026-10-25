package com.borarpg.domain.repository

import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.WorkoutMutation
import com.borarpg.domain.model.workout.WorkoutSession
import kotlinx.coroutines.flow.Flow

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
