package com.borarpg.feature.activity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.WorkoutMutation
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType
import com.borarpg.domain.repository.WorkoutRepository
import com.borarpg.domain.tracking.WorkoutTracker
import com.borarpg.feature.activity.presentation.action.WorkoutAction
import com.borarpg.feature.activity.presentation.state.WorkoutUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class WorkoutPresentationState(
    val mode: WorkoutType = WorkoutType.WALK,
    val historyId: String? = null,
    val busy: Boolean = false,
    val showHistory: Boolean = false,
    val showCancel: Boolean = false,
    val message: String? = null,
)

private data class WorkoutRecords(
    val sessions: List<WorkoutSession> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

private data class WorkoutSelection(
    val records: WorkoutRecords,
    val current: WorkoutSession?,
    val displayed: WorkoutSession?,
)

private data class WorkoutRoute(val id: String?, val points: List<RoutePoint> = emptyList())

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutViewModel
@Inject
constructor(
    private val repository: WorkoutRepository,
    private val tracker: WorkoutTracker,
) : ViewModel() {
    private val controls = MutableStateFlow(WorkoutPresentationState())
    private val retry = MutableStateFlow(0)
    private val records = retry.flatMapLatest {
        repository.workouts
            .map { WorkoutRecords(it, loading = false) }
            .catch {
                emit(
                    WorkoutRecords(
                        loading = false,
                        error = "Não foi possível carregar os percursos. Tente novamente.",
                    )
                )
            }
    }
    private val selection =
        combine(records, controls) { data, control ->
                val current = data.sessions.firstOrNull { it.status != WorkoutStatus.SAVED }
                val displayed =
                    current
                        ?: data.sessions.firstOrNull { it.id == control.historyId }
                        ?: data.sessions.firstOrNull()
                WorkoutSelection(data, current, displayed)
            }
            .distinctUntilChanged()
    private val route =
        combine(selection.map { it.displayed?.id }.distinctUntilChanged(), retry) { id, attempt ->
                id to attempt
            }
            .flatMapLatest { (id, _) ->
                if (id == null) flowOf(WorkoutRoute(null))
                else
                    repository
                        .observeRoute(id)
                        .map { WorkoutRoute(id, it) }
                        .onStart { emit(WorkoutRoute(id)) }
                        .catch {
                            controls.update {
                                it.copy(message = "Não foi possível carregar o percurso.")
                            }
                            emit(WorkoutRoute(id))
                        }
            }
    val state =
        combine(selection, route, tracker.state, controls) { selected, points, tracking, control ->
                WorkoutUiState(
                    loading = selected.records.loading,
                    mode = control.mode,
                    current = selected.current,
                    displayed = selected.displayed,
                    history = selected.records.sessions.filter { it.status == WorkoutStatus.SAVED },
                    historyId = control.historyId,
                    route = points.points.takeIf { points.id == selected.displayed?.id }.orEmpty(),
                    tracking = tracking,
                    busy = control.busy,
                    showHistory = control.showHistory,
                    showCancel = control.showCancel,
                    message = control.message ?: tracking.error ?: selected.records.error,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutUiState())

    fun onAction(action: WorkoutAction) {
        when (action) {
            is WorkoutAction.SelectMode ->
                if (state.value.current == null) controls.update { it.copy(mode = action.mode) }
            is WorkoutAction.SelectHistory ->
                if (state.value.current == null)
                    controls.update { it.copy(historyId = action.id, showHistory = false) }
            WorkoutAction.Follow -> controls.update { it.copy(historyId = null) }
            WorkoutAction.ShowHistory -> controls.update { it.copy(showHistory = true) }
            WorkoutAction.HideHistory -> controls.update { it.copy(showHistory = false) }
            WorkoutAction.ShowCancel -> controls.update { it.copy(showCancel = true) }
            WorkoutAction.HideCancel -> controls.update { it.copy(showCancel = false) }
            WorkoutAction.Retry -> {
                controls.update { it.copy(message = null) }
                retry.update { it + 1 }
            }
            WorkoutAction.Start ->
                if (!state.value.loading && state.value.current == null)
                    tracker.start(controls.value.mode)
            WorkoutAction.Pause ->
                if (state.value.current?.status == WorkoutStatus.ACTIVE) tracker.pause()
            WorkoutAction.Resume ->
                if (state.value.current?.status == WorkoutStatus.PAUSED) tracker.resume()
            WorkoutAction.Finish ->
                if (
                    state.value.current?.status in setOf(WorkoutStatus.ACTIVE, WorkoutStatus.PAUSED)
                )
                    tracker.finish()
            WorkoutAction.Cancel -> {
                controls.update { it.copy(showCancel = false) }
                if (
                    state.value.current?.status in setOf(WorkoutStatus.ACTIVE, WorkoutStatus.PAUSED)
                )
                    tracker.cancel()
            }
            WorkoutAction.Save -> mutate { repository.saveResult(it) }
            WorkoutAction.Discard -> mutate { repository.discard(it) }
        }
    }

    private fun mutate(operation: suspend (String) -> WorkoutMutation) {
        val current = state.value.current ?: return
        if (controls.value.busy || current.status != WorkoutStatus.DRAFT) return
        controls.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                val result = operation(current.id)
                controls.update {
                    it.copy(
                        message =
                            when (result) {
                                WorkoutMutation.APPLIED -> null
                                WorkoutMutation.NOT_FOUND ->
                                    "Esta atividade não está mais disponível."
                                WorkoutMutation.INVALID_STATE ->
                                    "O estado da atividade mudou. Confira o resultado antes de continuar."
                            }
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                controls.update {
                    it.copy(message = "Não foi possível atualizar a atividade. Tente novamente.")
                }
            } finally {
                controls.update { it.copy(busy = false) }
            }
        }
    }
}
