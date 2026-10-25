package com.borarpg.feature.activity.presentation.state

import com.borarpg.domain.model.location.GpsState
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.feature.missions.presentation.state.MissionState

internal data class ActivityUiState(
    val workout: WorkoutUiState,
    val mission: MissionState,
    val cep: CepState,
    val gps: GpsState,
    val savedQuests: List<QuestPoint>,
    val placesMessage: String?,
    val locationGranted: Boolean,
    val permissionRequested: Boolean,
) {
    val quests: List<QuestPoint>
        get() =
            (listOfNotNull(mission.active?.quest) + savedQuests + cep.objectives)
                .distinctBy { it.id }
                .filter { it.id !in mission.claimed }
}
