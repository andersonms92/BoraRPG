package com.borarpg.data.repository

import androidx.room.withTransaction
import com.borarpg.data.di.DefaultDispatcher
import com.borarpg.data.local.database.GameDatabase
import com.borarpg.data.mapper.toDomain
import com.borarpg.data.mapper.toEntity
import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.WorkoutMutation
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.repository.WorkoutRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@Singleton
internal class WorkoutRepositoryImpl
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
            dao.save(session.toEntity())
            dao.insertPoints(
                newPoints.mapIndexed { index, point ->
                    point.toEntity(session.id, startIndex + index)
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
