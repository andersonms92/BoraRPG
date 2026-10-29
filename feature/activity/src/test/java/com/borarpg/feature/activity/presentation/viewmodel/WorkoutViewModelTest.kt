package com.borarpg.feature.activity.presentation.viewmodel

import androidx.lifecycle.ViewModelStore
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.model.player.PlayerProfile
import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.TrackingPhase
import com.borarpg.domain.model.workout.TrackingState
import com.borarpg.domain.model.workout.WorkoutMutation
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType
import com.borarpg.domain.repository.PlayerRepository
import com.borarpg.domain.repository.WorkoutRepository
import com.borarpg.domain.tracking.WorkoutTracker
import com.borarpg.domain.usecase.player.ObservePlayerProfileUseCase
import com.borarpg.domain.usecase.workout.DiscardWorkoutResultUseCase
import com.borarpg.domain.usecase.workout.ObserveWorkoutRouteUseCase
import com.borarpg.domain.usecase.workout.ObserveWorkoutsUseCase
import com.borarpg.domain.usecase.workout.SaveWorkoutResultUseCase
import com.borarpg.feature.activity.presentation.action.WorkoutAction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val viewModels = ViewModelStore()
    private val observations = CoroutineScope(SupervisorJob() + dispatcher)

    private class Store : WorkoutRepository {
        override val workouts = MutableStateFlow(emptyList<WorkoutSession>())
        val routes = MutableStateFlow(emptyMap<String, List<RoutePoint>>())
        var routeRequests = 0
        var failRoute = false
        var calls = 0
        var result = WorkoutMutation.APPLIED
        var gate: CompletableDeferred<Unit>? = null
        var throwOnSave = false

        override fun observeRoute(id: String): Flow<List<RoutePoint>> {
            routeRequests++
            return if (failRoute) flow { error("route unavailable") }
            else routes.map { it[id].orEmpty() }
        }

        override suspend fun current() =
            workouts.value.firstOrNull { it.status != WorkoutStatus.SAVED }

        override suspend fun checkpoint(
            session: WorkoutSession,
            newPoints: List<RoutePoint>,
            startIndex: Int,
        ) = Unit

        override suspend fun delete(id: String) = Unit

        override suspend fun recoverInterruptedSessions() = Unit

        override suspend fun saveResult(id: String): WorkoutMutation {
            calls++
            gate?.await()
            if (throwOnSave) error("storage")
            if (result == WorkoutMutation.APPLIED)
                workouts.update { list ->
                    list.map { if (it.id == id) it.copy(status = WorkoutStatus.SAVED) else it }
                }
            return result
        }

        override suspend fun discard(id: String) = WorkoutMutation.APPLIED.also { calls++ }
    }

    private class Tracker : WorkoutTracker {
        override val state = MutableStateFlow(TrackingState())
        val commands = mutableListOf<String>()

        override fun start(type: WorkoutType) {
            commands += type.name
        }

        override fun pause() {
            commands += "pause"
        }

        override fun resume() {
            commands += "resume"
        }

        override fun finish() {
            commands += "finish"
        }

        override fun cancel() {
            commands += "cancel"
        }
    }

    private val repository = Store()
    private val tracker = Tracker()
    private val player = PlayerStore()

    private class PlayerStore : PlayerRepository {
        val profiles = MutableStateFlow(PlayerProfile())
        var fail = false
        override val profile: Flow<PlayerProfile>
            get() = if (fail) flow { error("profile unavailable") } else profiles

        override val quests = MutableStateFlow(emptyList<QuestPoint>())

        override suspend fun save(profile: PlayerProfile) {
            profiles.value = profile
        }

        override suspend fun addQuest(quest: QuestPoint) = Unit

        override suspend fun deleteQuest(id: String) = Unit
    }

    private fun session(id: String = "session", status: WorkoutStatus = WorkoutStatus.DRAFT) =
        WorkoutSession(id, WorkoutType.RUN, 1000, 30_000, 100.0, 65.0, status, emptyList())

    private fun model() =
        WorkoutViewModel(
            ObserveWorkoutsUseCase(repository),
            ObserveWorkoutRouteUseCase(repository),
            SaveWorkoutResultUseCase(repository),
            DiscardWorkoutResultUseCase(repository),
            tracker,
            ObservePlayerProfileUseCase(player),
        ).also { vm ->
            viewModels.put("workout", vm)
            observations.launch { vm.state.collect() }
        }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun cleanup() {
        observations.cancel()
        viewModels.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun startUsesSelectedModeAndIgnoresIncompatibleActions() = runTest {
        val vm = model()
        advanceUntilIdle()
        vm.onAction(WorkoutAction.SelectMode(WorkoutType.RUN))
        vm.onAction(WorkoutAction.Start)
        vm.onAction(WorkoutAction.Pause)
        vm.onAction(WorkoutAction.Resume)
        assertEquals(listOf("RUN"), tracker.commands)
    }

    @Test
    fun pausedSessionResumesAndCannotStartAnother() = runTest {
        repository.workouts.value = listOf(session(status = WorkoutStatus.PAUSED))
        val vm = model()
        advanceUntilIdle()
        vm.onAction(WorkoutAction.Start)
        vm.onAction(WorkoutAction.Resume)
        vm.onAction(WorkoutAction.Finish)
        assertEquals(listOf("resume", "finish"), tracker.commands)
    }

    @Test
    fun duplicateSaveIsIgnoredWhileStorageIsBusy() = runTest {
        repository.workouts.value = listOf(session())
        repository.gate = CompletableDeferred()
        val vm = model()
        advanceUntilIdle()
        vm.onAction(WorkoutAction.Save)
        vm.onAction(WorkoutAction.Save)
        runCurrent()
        assertEquals(1, repository.calls)
        assertTrue(vm.state.value.busy)
        repository.gate!!.complete(Unit)
        advanceUntilIdle()
        assertFalse(vm.state.value.busy)
        assertNull(vm.state.value.current)
        assertEquals(1, vm.state.value.history.size)
    }

    @Test
    fun invalidStateIsReportedWithoutClaimingSuccess() = runTest {
        repository.workouts.value = listOf(session())
        repository.result = WorkoutMutation.INVALID_STATE
        val vm = model()
        advanceUntilIdle()
        vm.onAction(WorkoutAction.Save)
        advanceUntilIdle()
        assertTrue(vm.state.value.message!!.contains("estado"))
        assertEquals(WorkoutStatus.DRAFT, vm.state.value.current!!.status)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun storageFailureAllowsRetry() = runTest {
        repository.workouts.value = listOf(session())
        repository.throwOnSave = true
        val vm = model()
        advanceUntilIdle()
        vm.onAction(WorkoutAction.Save)
        advanceUntilIdle()
        assertNotNull(vm.state.value.message)
        repository.throwOnSave = false
        vm.onAction(WorkoutAction.Save)
        advanceUntilIdle()
        assertEquals(2, repository.calls)
        assertNull(vm.state.value.message)
    }

    @Test
    fun selectingHistoryLoadsOnlyThatRouteAndIgnoresOldUpdates() = runTest {
        val a = RoutePoint(0.0, 0.0, 1000, 0)
        val b = RoutePoint(1.0, 1.0, 2000, 0)
        repository.workouts.value =
            listOf(session("a", WorkoutStatus.SAVED), session("b", WorkoutStatus.SAVED))
        repository.routes.value = mapOf("a" to listOf(a), "b" to listOf(b))
        val vm = model()
        advanceUntilIdle()
        assertEquals(listOf(a), vm.state.value.route)
        vm.onAction(WorkoutAction.SelectHistory("b"))
        advanceUntilIdle()
        assertEquals("b", vm.state.value.displayed!!.id)
        assertEquals(listOf(b), vm.state.value.route)
        repository.routes.update { it + ("a" to emptyList()) }
        advanceUntilIdle()
        assertEquals(listOf(b), vm.state.value.route)
    }

    @Test
    fun elapsedTimeUsesOnlyTheMatchingSession() = runTest {
        repository.workouts.value = listOf(session(status = WorkoutStatus.ACTIVE))
        tracker.state.value = TrackingState(TrackingPhase.RUNNING, "other", elapsedMillis = 90_000)
        val vm = model()
        advanceUntilIdle()
        assertEquals(30_000L, vm.state.value.elapsedMillis)
        tracker.state.value = tracker.state.value.copy(sessionId = "session")
        advanceUntilIdle()
        assertEquals(90_000L, vm.state.value.elapsedMillis)
    }

    @Test
    fun retryResubscribesToAFailedRouteWithoutChangingSelection() = runTest {
        repository.workouts.value = listOf(session("a", WorkoutStatus.SAVED))
        repository.failRoute = true
        val vm = model()
        advanceUntilIdle()
        assertNotNull(vm.state.value.message)
        val point = RoutePoint(0.0, 0.0, 1000, 0)
        repository.routes.value = mapOf("a" to listOf(point))
        repository.failRoute = false
        vm.onAction(WorkoutAction.Retry)
        advanceUntilIdle()
        assertEquals(listOf(point), vm.state.value.route)
        assertEquals(2, repository.routeRequests)
        assertNull(vm.state.value.message)
    }

    @Test
    fun editingProfileRecalculatesSavedActivityWithoutRecordedWeight() = runTest {
        val saved = session(status = WorkoutStatus.SAVED).copy(weightKg = null)
        repository.workouts.value = listOf(saved)
        val vm = model()
        advanceUntilIdle()
        assertNull(vm.state.value.calories)

        player.profiles.value = PlayerProfile(weightKg = 80.0)
        advanceUntilIdle()
        assertEquals(5.6666667, vm.state.value.calories!!, .0001)
        assertTrue(vm.state.value.caloriesUseCurrentWeight)
        assertNull(repository.workouts.value.single().weightKg)
        assertEquals(saved.id, vm.state.value.displayed!!.id)
    }

    @Test
    fun unavailableProfileDoesNotHideSavedActivities() = runTest {
        repository.workouts.value = listOf(session(status = WorkoutStatus.SAVED))
        player.fail = true
        val vm = model()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.history.size)
        assertNotNull(vm.state.value.calories)
        assertFalse(vm.state.value.caloriesUseCurrentWeight)
    }
}
