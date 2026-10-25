package com.borarpg.feature.activity.presentation.action

import com.borarpg.domain.model.mission.QuestPoint

internal sealed interface ActivityAction {
    data class Workout(val action: WorkoutAction) : ActivityAction

    data class AcceptMission(val quest: QuestPoint) : ActivityAction

    data object ClaimMission : ActivityAction

    data object CancelMission : ActivityAction

    data class CreateQuest(val title: String, val latitude: Double, val longitude: Double) :
        ActivityAction

    data class AddQuest(val quest: QuestPoint) : ActivityAction

    data class DeleteQuest(val id: String) : ActivityAction

    data class EditCep(val input: String) : ActivityAction

    data object SearchCep : ActivityAction

    data object CancelCep : ActivityAction

    data object ClearCepTarget : ActivityAction

    data object RegenerateObjectives : ActivityAction

    data object RequestLocation : ActivityAction

    data object RefreshLocationPermission : ActivityAction

    data object OpenPermissions : ActivityAction

    data object OpenLocationSettings : ActivityAction

    data object OpenProfile : ActivityAction
}
