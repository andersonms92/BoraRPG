package com.borarpg.domain.usecase.mission

import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.repository.MissionRepository
import com.borarpg.domain.rules.mission.MissionRules

class AcceptMissionUseCase(private val repository: MissionRepository) {
    suspend operator fun invoke(quest: QuestPoint) {
        MissionRules.validate(quest)
        repository.accept(quest)
    }
}
