package com.borarpg.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.*
import com.borarpg.domain.WorkoutType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkoutViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = DatabaseProvider.get(app).workoutDao()
    private val controller = TrackingController(app)
    val sensorAvailable = controller.available
    val tracking = controller.state
    val workouts =
        dao.observe()
            .catch {
                message.value = "Não foi possível carregar os percursos."
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val message = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            try {
                tracking.collect { if (!it.active) dao.recover() }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message.value = "Não foi possível recuperar a atividade anterior."
            }
        }
    }

    fun start(type: WorkoutType) = controller.start(type)

    fun pause() = controller.pause()

    fun resume() = controller.resume()

    fun finish() = controller.stop()

    fun cancel() = controller.cancel()

    fun save(result: WorkoutEntity) {
        viewModelScope.launch {
            try {
                if (result.status == "draft") dao.save(result.copy(status = "saved"))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message.value = "Não foi possível salvar. Tente novamente."
            }
        }
    }

    fun discard(result: WorkoutEntity) {
        viewModelScope.launch {
            try {
                if (result.status == "draft" || !tracking.value.active) dao.delete(result.id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message.value = "Não foi possível descartar. Tente novamente."
            }
        }
    }
}
