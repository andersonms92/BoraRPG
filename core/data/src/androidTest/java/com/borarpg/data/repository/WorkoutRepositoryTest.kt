package com.borarpg.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.borarpg.data.local.dao.WorkoutDao
import com.borarpg.data.local.database.GameDatabase
import com.borarpg.data.local.entity.WorkoutEntity
import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.WorkoutMutation
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val route =
        listOf(RoutePoint(-23.55, -46.63, 1000, 0), RoutePoint(-23.551, -46.63, 5000, 1))

    private suspend fun withStore(test: suspend (WorkoutDao, RoomWorkoutRepository) -> Unit) {
        val db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        try {
            test(db.workoutDao(), RoomWorkoutRepository(db, Dispatchers.Default))
        } finally {
            db.close()
        }
    }

    private fun session(status: WorkoutStatus = WorkoutStatus.ACTIVE) =
        WorkoutSession("result", WorkoutType.RUN, 1000, 60_000, 250.0, 72.0, status, emptyList())

    @Test
    fun historyContainsSummariesAndLoadsRouteOnDemand() = runBlocking {
        withStore { dao, repository ->
            dao.save(WorkoutEntity("old", "WALK", 0, status = "saved"))
            repository.checkpoint(session(WorkoutStatus.DRAFT), route, 0)
            val summaries = repository.workouts.first()
            assertEquals(listOf("result", "old"), summaries.map { it.id })
            assertTrue(summaries.all { it.route.isEmpty() })
            assertEquals(route, repository.observeRoute("result").first())
            assertEquals(route, repository.current()!!.route)
        }
    }

    @Test
    fun savingByIdPreservesUpdatesMadeAfterTheScreenSnapshot() = runBlocking {
        withStore { _, repository ->
            repository.checkpoint(session(), route.take(1), 0)
            val snapshot = repository.workouts.first().single()
            repository.checkpoint(
                session(WorkoutStatus.DRAFT).copy(elapsedMillis = 90_000),
                route.drop(1),
                1,
            )
            assertEquals(WorkoutMutation.APPLIED, repository.saveResult(snapshot.id))
            val saved = repository.workouts.first().single()
            assertEquals(WorkoutStatus.SAVED, saved.status)
            assertEquals(90_000L, saved.elapsedMillis)
            assertEquals(route, repository.observeRoute(saved.id).first())
        }
    }

    @Test
    fun onlyFinalizedResultsCanBeSaved() = runBlocking {
        withStore { dao, repository ->
            for (status in listOf("active", "paused", "draft", "saved")) {
                dao.save(WorkoutEntity(status, "WALK", 1000, status = status))
                assertEquals(
                    if (status == "draft") WorkoutMutation.APPLIED
                    else WorkoutMutation.INVALID_STATE,
                    repository.saveResult(status),
                )
            }
        }
    }

    @Test
    fun discardPreservesActiveAndSavedSessions() = runBlocking {
        withStore { dao, repository ->
            for (status in listOf("active", "paused", "draft", "saved")) {
                dao.save(WorkoutEntity(status, "WALK", 1000, status = status))
                repository.discard(status)
            }
            assertEquals(
                setOf("active", "saved"),
                repository.workouts.first().map { it.id }.toSet(),
            )
        }
    }

    @Test
    fun staleActionsReturnNotFoundAndDoNotRecreateDeletedWorkout() = runBlocking {
        withStore { _, repository ->
            repository.checkpoint(session(WorkoutStatus.DRAFT), route, 0)
            assertEquals(WorkoutMutation.APPLIED, repository.discard("result"))
            assertEquals(WorkoutMutation.NOT_FOUND, repository.saveResult("result"))
            assertEquals(WorkoutMutation.NOT_FOUND, repository.discard("result"))
            assertTrue(repository.workouts.first().isEmpty())
        }
    }

    @Test
    fun recoveryOnlyPausesInterruptedSessions() = runBlocking {
        withStore { dao, repository ->
            for (status in listOf("active", "paused", "draft", "saved")) dao.save(
                WorkoutEntity(status, "WALK", 1000, 45_000, status = status)
            )
            repository.recoverInterruptedSessions()
            val states = repository.workouts.first().associate { it.id to it.status }
            assertEquals(WorkoutStatus.PAUSED, states["active"])
            assertEquals(WorkoutStatus.PAUSED, states["paused"])
            assertEquals(WorkoutStatus.DRAFT, states["draft"])
            assertEquals(WorkoutStatus.SAVED, states["saved"])
            assertTrue(repository.workouts.first().all { it.elapsedMillis == 45_000L })
        }
    }

    @Test
    fun checkpointsAppendOnlyNewPointsAndDeletionCascades() = runBlocking {
        withStore { dao, repository ->
            repository.checkpoint(session(), route.take(1), 0)
            repeat(3) { repository.checkpoint(session(), emptyList(), 1) }
            repository.checkpoint(session(WorkoutStatus.PAUSED), route.drop(1), 1)
            assertEquals(route, repository.observeRoute("result").first())
            repository.discard("result")
            assertTrue(dao.points("result").isEmpty())
        }
    }

    @Test
    fun failedPointBatchRollsBackTheSessionSummary() = runBlocking {
        withStore { _, repository ->
            repository.checkpoint(session(), route.take(1), 0)
            assertTrue(
                runCatching {
                    repository.checkpoint(session().copy(distanceMeters = 999.0), route, 0)
                }
                    .isFailure
            )
            assertEquals(250.0, repository.current()!!.distanceMeters, 0.0)
            assertEquals(route.take(1), repository.observeRoute("result").first())
        }
    }

    @Test
    fun savedResultCannotBeOverwrittenByLateCheckpoint() = runBlocking {
        withStore { _, repository ->
            repository.checkpoint(session(WorkoutStatus.DRAFT), route, 0)
            repository.saveResult("result")
            assertTrue(runCatching { repository.checkpoint(session(), emptyList(), 2) }.isFailure)
            assertEquals(WorkoutStatus.SAVED, repository.workouts.first().single().status)
        }
    }
}
