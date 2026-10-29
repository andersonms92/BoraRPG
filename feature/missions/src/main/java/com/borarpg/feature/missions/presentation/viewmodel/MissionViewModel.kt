package com.borarpg.feature.missions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.usecase.mission.AcceptMissionUseCase
import com.borarpg.domain.usecase.mission.CancelMissionUseCase
import com.borarpg.domain.usecase.mission.ClaimMissionRewardsUseCase
import com.borarpg.domain.usecase.mission.ObserveMissionsUseCase
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
class MissionViewModel
@Inject
constructor(
    observeMissions: ObserveMissionsUseCase,
    private val acceptMission: AcceptMissionUseCase,
    private val claimMissionRewards: ClaimMissionRewardsUseCase,
    private val cancelMission: CancelMissionUseCase,
) : ViewModel() {
    private val action = MutableStateFlow(MissionAction())
    val state =
        combine(observeMissions(), action) { missions, status ->
                MissionState(
                    missions.active,
                    missions.inventory,
                    missions.claimed,
                    status.busy,
                    status.message,
                )
            }
            .catch { emit(MissionState(message = "Não foi possível carregar as missões.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MissionState())

    fun accept(quest: QuestPoint) =
        runAction("Missão aceita. Inicie uma atividade na aba Atividade para avançar.") {
            acceptMission(quest)
        }

    fun claim() =
        runAction("Recompensas recebidas! XP, ouro e item adicionados ao personagem.") {
            claimMissionRewards()
        }

    fun cancel() = runAction("Missão cancelada.") { cancelMission() }

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
