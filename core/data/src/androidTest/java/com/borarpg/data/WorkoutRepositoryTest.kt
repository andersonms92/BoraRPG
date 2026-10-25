package com.borarpg.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.borarpg.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val route =
        listOf(RoutePoint(-23.55, -46.63, 1000, 0), RoutePoint(-23.551, -46.63, 5000, 1))

    private suspend fun withStore(test: suspend (WorkoutDao, WorkoutRepository) -> Unit) {
        val db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        try {
            test(db.workoutDao(), WorkoutRepository(db.workoutDao()))
        } finally {
            db.close()
        }
    }

    @Test
    fun mapsStoredWorkoutsToDomainModelsInHistoryOrder() = runBlocking {
        withStore { dao, repository ->
            dao.save(WorkoutEntity("old", "WALK", 1000, status = "saved"))
            dao.save(
                WorkoutEntity("new", "RUN", 2000, 60000, 250.0, 72.0, "draft", encodeRoute(route))
            )
            val workouts = repository.workouts.first()
            assertEquals(listOf("new", "old"), workouts.map { it.id })
            assertEquals(
                WorkoutSession(
                    "new",
                    WorkoutType.RUN,
                    2000,
                    60000,
                    250.0,
                    72.0,
                    WorkoutStatus.DRAFT,
                    route,
                ),
                workouts.first(),
            )
            assertEquals(WorkoutStatus.SAVED, workouts.last().status)
        }
    }

    @Test
    fun savingByIdPreservesUpdatesMadeAfterTheScreenSnapshot() = runBlocking {
        withStore { dao, repository ->
            val initial = WorkoutEntity("result", "RUN", 1000, 30000, 50.0, 72.0, "draft")
            dao.save(initial)
            val screenSnapshot = repository.workouts.first().single()
            val latest =
                initial.copy(
                    elapsedMillis = 45000,
                    distanceMeters = 100.0,
                    routeJson = encodeRoute(route),
                )
            dao.save(latest)
            repository.saveResult(screenSnapshot.id)
            val saved = repository.workouts.first().single()
            assertEquals(WorkoutStatus.SAVED, saved.status)
            assertEquals(45000L, saved.elapsedMillis)
            assertEquals(100.0, saved.distanceMeters, 0.0)
            assertEquals(route, saved.route)
        }
    }

    @Test
    fun onlyFinalizedResultsCanBeSaved() = runBlocking {
        withStore { dao, repository ->
            for (status in listOf("active", "paused", "draft", "saved")) {
                dao.save(WorkoutEntity(status, "WALK", 1000, status = status))
                repository.saveResult(status)
            }
            val states = repository.workouts.first().associate { it.id to it.status }
            assertEquals(WorkoutStatus.ACTIVE, states["active"])
            assertEquals(WorkoutStatus.PAUSED, states["paused"])
            assertEquals(WorkoutStatus.SAVED, states["draft"])
            assertEquals(WorkoutStatus.SAVED, states["saved"])
        }
    }

    @Test
    fun discardRemovesDraftAndPausedSessionsButPreservesActiveAndSaved() = runBlocking {
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
    fun staleActionsDoNotRecreateADeletedWorkout() = runBlocking {
        withStore { dao, repository ->
            dao.save(WorkoutEntity("deleted", "WALK", 1000, status = "draft"))
            val screenSnapshot = repository.workouts.first().single()
            repository.discard(screenSnapshot.id)
            repository.saveResult(screenSnapshot.id)
            repository.discard(screenSnapshot.id)
            assertTrue(repository.workouts.first().isEmpty())
        }
    }

    @Test
    fun recoveryOnlyPausesInterruptedSessionsAndKeepsRecordedData() = runBlocking {
        withStore { dao, repository ->
            for (status in listOf("active", "paused", "draft", "saved")) {
                dao.save(
                    WorkoutEntity(
                        status,
                        "WALK",
                        1000,
                        45000,
                        100.0,
                        72.0,
                        status,
                        encodeRoute(route),
                    )
                )
            }
            repository.recoverInterruptedSessions()
            val workouts = repository.workouts.first()
            val states = workouts.associate { it.id to it.status }
            assertEquals(WorkoutStatus.PAUSED, states["active"])
            assertEquals(WorkoutStatus.PAUSED, states["paused"])
            assertEquals(WorkoutStatus.DRAFT, states["draft"])
            assertEquals(WorkoutStatus.SAVED, states["saved"])
            workouts.forEach {
                assertEquals(45000L, it.elapsedMillis)
                assertEquals(100.0, it.distanceMeters, 0.0)
                assertEquals(72.0, it.weightKg!!, 0.0)
                assertEquals(route, it.route)
            }
        }
    }
}
