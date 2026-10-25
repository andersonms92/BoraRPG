package com.borarpg.data

import com.borarpg.domain.RoutePoint
import com.borarpg.domain.WorkoutSession
import com.borarpg.domain.WorkoutStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@Singleton
class WorkoutRepository @Inject constructor(private val dao: WorkoutDao) {
    private data class CachedRoute(val json: String, val points: List<RoutePoint>)

    val workouts = flow {
        val routes = mutableMapOf<String, CachedRoute>()
        dao.observe().collect { records ->
            val sessions = records.map { record ->
                val cached = routes[record.id]?.takeIf { it.json == record.routeJson }
                val route =
                    cached
                        ?: CachedRoute(record.routeJson, record.points()).also {
                            routes[record.id] = it
                        }
                record.toDomain(route.points)
            }
            routes.keys.retainAll(records.map { it.id }.toSet())
            emit(sessions)
        }
    }
        .flowOn(Dispatchers.Default)

    suspend fun recoverInterruptedSessions() = dao.recover()

    suspend fun saveResult(id: String) = dao.saveResult(id)

    suspend fun discard(id: String) = dao.discard(id)

    private fun WorkoutEntity.toDomain(route: List<RoutePoint>) =
        WorkoutSession(
            id = id,
            mode = mode,
            startedAt = startedAt,
            elapsedMillis = elapsedMillis,
            distanceMeters = distanceMeters,
            weightKg = weightKg,
            status =
                when (status) {
                    "active" -> WorkoutStatus.ACTIVE
                    "paused" -> WorkoutStatus.PAUSED
                    "draft" -> WorkoutStatus.DRAFT
                    "saved" -> WorkoutStatus.SAVED
                    else -> error("Estado de atividade inválido: $status")
                },
            route = route,
        )
}
