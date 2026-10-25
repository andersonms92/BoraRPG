package com.borarpg.domain

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class WorkoutSessionManagerTest {
    private class Time : WorkoutTimeSource {
        var now = 100_000L

        override fun monotonicMillis() = now

        override fun epochMillis() = 1_000_000L + now
    }

    private class Store : WorkoutRepository {
        var session: WorkoutSession? = null
        var fail = false
        val points = mutableListOf<RoutePoint>()
        override val workouts = MutableStateFlow(emptyList<WorkoutSession>())

        override fun observeRoute(id: String) = flowOf(points.toList())

        override suspend fun current() = session?.copy(route = points.toList())

        override suspend fun checkpoint(
            session: WorkoutSession,
            newPoints: List<RoutePoint>,
            startIndex: Int,
        ) {
            if (fail) error("disk unavailable")
            assertEquals(points.size, startIndex)
            this.session = session
            points.addAll(newPoints)
        }

        override suspend fun delete(id: String) {
            session = null
            points.clear()
        }

        override suspend fun recoverInterruptedSessions() {
            session = session?.copy(status = WorkoutStatus.PAUSED)
        }

        override suspend fun saveResult(id: String) = WorkoutMutation.APPLIED

        override suspend fun discard(id: String) = WorkoutMutation.APPLIED
    }

    private class Player : PlayerRepository {
        override val profile = flowOf(PlayerProfile("Ana", 65.0, 170.0, 30))
        override val quests = flowOf(emptyList<QuestPoint>())

        override suspend fun save(profile: PlayerProfile) = Unit

        override suspend fun addQuest(quest: QuestPoint) = Unit

        override suspend fun deleteQuest(id: String) = Unit
    }

    private class Adventure : AdventureRepository {
        var steps = 0
        override val hero = flowOf(Hero())

        override suspend fun recordSteps(steps: Int) {
            this.steps += steps
        }
    }

    private val time = Time()
    private val store = Store()
    private val adventure = Adventure()
    private val manager = WorkoutSessionManager(store, Player(), adventure, time) { "session" }

    @Test
    fun pauseAndResumeExcludeStoppedTimeAndDistance() = runTest {
        manager.start(WorkoutType.WALK)
        manager.location(GeoLocation(0.0, 0.0, 5f), time.now)
        time.now += 10_000
        manager.location(GeoLocation(0.0001, 0.0, 5f), time.now)
        manager.pause()
        val distance = store.session!!.distanceMeters
        time.now += 100_000
        manager.resume()
        manager.location(GeoLocation(1.0, 1.0, 5f), time.now)
        time.now += 5_000
        manager.finish()
        assertEquals(15_000L, store.session!!.elapsedMillis)
        assertEquals(distance, store.session!!.distanceMeters, .001)
        assertEquals(listOf(0, 0, 1), store.points.map { it.segment })
        assertEquals(WorkoutStatus.DRAFT, store.session!!.status)
        assertEquals(TrackingPhase.IDLE, manager.state.value.phase)
    }

    @Test
    fun rejectsInaccurateStaleFutureAndImpossibleLocations() = runTest {
        manager.start(WorkoutType.WALK)
        manager.location(GeoLocation(0.0, 0.0, 36f), time.now)
        manager.location(GeoLocation(0.0, 0.0, 5f), time.now - 15_001)
        manager.location(GeoLocation(0.0, 0.0, 5f), time.now + 1)
        manager.location(GeoLocation(Double.NaN, 0.0, 5f), time.now)
        assertTrue(store.points.isEmpty())
        manager.location(GeoLocation(0.0, 0.0, 5f), time.now)
        time.now += 1000
        manager.location(GeoLocation(1.0, 1.0, 5f), time.now)
        assertEquals(1, store.points.size)
        assertEquals(0.0, store.session!!.distanceMeters, 0.0)
    }

    @Test
    fun gpsGapStartsAnotherSegmentWithoutConnectingDistance() = runTest {
        manager.start(WorkoutType.RUN)
        manager.location(GeoLocation(0.0, 0.0, 5f), time.now)
        time.now += 61_000
        manager.location(GeoLocation(1.0, 1.0, 5f), time.now)
        assertEquals(listOf(0, 1), store.points.map { it.segment })
        assertEquals(0.0, store.session!!.distanceMeters, 0.0)
    }

    @Test
    fun finishingRecoveredSessionDoesNotCountTimeOutsideTheApp() = runTest {
        store.session =
            WorkoutSession(
                "old",
                WorkoutType.RUN,
                1000,
                45_000,
                100.0,
                65.0,
                WorkoutStatus.PAUSED,
                emptyList(),
            )
        manager.finish()
        assertEquals(45_000L, store.session!!.elapsedMillis)
        assertEquals(WorkoutStatus.DRAFT, store.session!!.status)
    }

    @Test
    fun cannotStartWhileAnotherSessionAwaitsCompletion() = runTest {
        store.session =
            WorkoutSession(
                "old",
                WorkoutType.WALK,
                1000,
                0,
                0.0,
                null,
                WorkoutStatus.DRAFT,
                emptyList(),
            )
        assertTrue(runCatching { manager.start(WorkoutType.RUN) }.isFailure)
        assertEquals("old", store.session!!.id)
    }

    @Test
    fun repeatedCommandsAndTicksDoNotDuplicatePoints() = runTest {
        manager.start(WorkoutType.WALK)
        manager.start(WorkoutType.RUN)
        manager.resume()
        manager.location(GeoLocation(0.0, 0.0, 5f), time.now)
        repeat(5) {
            time.now += 1000
            manager.tick()
        }
        manager.pause()
        manager.pause()
        assertEquals(1, store.points.size)
        assertEquals(5000L, store.session!!.elapsedMillis)
        assertEquals(WorkoutType.WALK, store.session!!.mode)
    }

    @Test
    fun failedCheckpointDoesNotAdvanceThePointCursor() = runTest {
        manager.start(WorkoutType.WALK)
        store.fail = true
        assertTrue(runCatching { manager.location(GeoLocation(0.0, 0.0, 5f), time.now) }.isFailure)
        store.fail = false
        manager.tick()
        assertEquals(1, store.points.size)
    }

    @Test
    fun stepsUseANewBaselineAfterResumeAndCancelKeepsRewards() = runTest {
        manager.start(WorkoutType.WALK)
        manager.steps(100)
        manager.steps(110)
        manager.pause()
        manager.steps(150)
        manager.resume()
        manager.steps(200)
        manager.steps(205)
        manager.cancel()
        assertEquals(15, adventure.steps)
        assertNull(store.session)
        assertEquals(TrackingPhase.IDLE, manager.state.value.phase)
    }

    @Test
    fun gpsWarningSurvivesTicksUntilAValidLocationArrives() = runTest {
        manager.start(WorkoutType.WALK)
        manager.locationUnavailable()
        time.now += 1000
        manager.tick()
        assertNotNull(manager.state.value.error)
        manager.location(GeoLocation(0.0, 0.0, 5f), time.now)
        assertNull(manager.state.value.error)
    }
}
