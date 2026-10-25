package com.borarpg.feature.activity

import androidx.lifecycle.ViewModelStore
import com.borarpg.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
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

    private fun session(id: String = "session", status: WorkoutStatus = WorkoutStatus.DRAFT) =
        WorkoutSession(id, WorkoutType.RUN, 1000, 30_000, 100.0, 65.0, status, emptyList())

    private fun model() =
        WorkoutViewModel(repository, tracker).also { vm ->
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
}
