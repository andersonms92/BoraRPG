package com.borarpg.data

import androidx.room.withTransaction
import com.borarpg.data.di.DefaultDispatcher
import com.borarpg.domain.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@Singleton
class RoomWorkoutRepository
@Inject
constructor(
    private val db: GameDatabase,
    @param:DefaultDispatcher private val computation: CoroutineDispatcher,
) : WorkoutRepository {
    private val dao = db.workoutDao()
    override val workouts = dao.observe().map { records -> records.map { it.toDomain() } }

    override fun observeRoute(id: String) =
        dao.observePoints(id).map { points -> points.map { it.toDomain() } }.flowOn(computation)

    override suspend fun current(): WorkoutSession? = db.withTransaction {
        dao.current()?.let { it.toDomain(dao.points(it.id).map { point -> point.toDomain() }) }
    }

    override suspend fun checkpoint(
        session: WorkoutSession,
        newPoints: List<RoutePoint>,
        startIndex: Int,
    ) {
        db.withTransaction {
            val stored = dao.find(session.id)
            check(stored == null || stored.status in setOf("active", "paused"))
            check(stored != null || startIndex == 0)
            dao.save(
                WorkoutEntity(
                    session.id,
                    session.mode.name,
                    session.startedAt,
                    session.elapsedMillis,
                    session.distanceMeters,
                    session.weightKg,
                    session.status.name.lowercase(),
                )
            )
            dao.insertPoints(
                newPoints.mapIndexed { index, point ->
                    WorkoutPointEntity(
                        session.id,
                        startIndex + index,
                        point.latitude,
                        point.longitude,
                        point.time,
                        point.segment,
                    )
                }
            )
        }
    }

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun recoverInterruptedSessions() = dao.recover()

    override suspend fun saveResult(id: String): WorkoutMutation = db.withTransaction {
        when {
            dao.saveResult(id) == 1 -> WorkoutMutation.APPLIED
            dao.find(id) == null -> WorkoutMutation.NOT_FOUND
            else -> WorkoutMutation.INVALID_STATE
        }
    }

    override suspend fun discard(id: String): WorkoutMutation = db.withTransaction {
        when {
            dao.discard(id) == 1 -> WorkoutMutation.APPLIED
            dao.find(id) == null -> WorkoutMutation.NOT_FOUND
            else -> WorkoutMutation.INVALID_STATE
        }
    }
}
