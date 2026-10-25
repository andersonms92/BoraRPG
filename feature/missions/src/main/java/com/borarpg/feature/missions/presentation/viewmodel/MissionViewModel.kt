package com.borarpg.feature.missions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.repository.MissionRepository
import com.borarpg.feature.missions.presentation.state.MissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class MissionAction(val busy: Boolean = false, val message: String? = null)

@HiltViewModel
class MissionViewModel @Inject constructor(private val repository: MissionRepository) :
    ViewModel() {
    private val action = MutableStateFlow(MissionAction())
    val state =
        combine(repository.active, repository.inventory, repository.claimed, action) {
                active,
                inventory,
                claimed,
                status ->
                MissionState(active, inventory, claimed, status.busy, status.message)
            }
            .catch { emit(MissionState(message = "Não foi possível carregar as missões.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MissionState())

    fun accept(quest: QuestPoint) =
        runAction("Missão aceita. Inicie uma atividade na aba Atividade para avançar.") {
            repository.accept(quest)
        }

    fun claim() =
        runAction("Recompensas recebidas! XP, ouro e item adicionados ao personagem.") {
            repository.claim()
        }

    fun cancel() = runAction("Missão cancelada.") { repository.cancel() }

    private fun runAction(message: String, operation: suspend () -> Unit) {
        if (action.value.busy) return
        action.value = MissionAction(busy = true)
        viewModelScope.launch {
            try {
                operation()
                action.value = MissionAction(message = message)
            } catch (error: CancellationException) {
                throw error
            } catch (error: IllegalStateException) {
                action.value = MissionAction(message = error.message)
            } catch (_: Exception) {
                action.value =
                    MissionAction(message = "Não foi possível salvar a missão. Tente novamente.")
            }
        }
    }
}
