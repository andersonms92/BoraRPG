package com.borarpg.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.WorkoutRepository
import com.borarpg.domain.WorkoutSession
import com.borarpg.domain.WorkoutType
import com.borarpg.tracking.TrackingController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel
@Inject
constructor(private val repository: WorkoutRepository, private val controller: TrackingController) :
    ViewModel() {
    val sensorAvailable = controller.available
    val tracking = controller.state
    private val mutableMessage = MutableStateFlow<String?>(null)
    val message = mutableMessage.asStateFlow()
    val workouts =
        repository.workouts
            .catch {
                mutableMessage.value = "Não foi possível carregar os percursos."
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                tracking.collect { if (!it.active) repository.recoverInterruptedSessions() }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableMessage.value = "Não foi possível recuperar a atividade anterior."
            }
        }
    }

    fun start(type: WorkoutType) = controller.start(type)

    fun pause() = controller.pause()

    fun resume() = controller.resume()

    fun finish() = controller.stop()

    fun cancel() = controller.cancel()

    fun save(result: WorkoutSession) {
        viewModelScope.launch {
            try {
                repository.saveResult(result.id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableMessage.value = "Não foi possível salvar. Tente novamente."
            }
        }
    }

    fun discard(result: WorkoutSession) {
        viewModelScope.launch {
            try {
                repository.discard(result.id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableMessage.value = "Não foi possível descartar. Tente novamente."
            }
        }
    }
}
