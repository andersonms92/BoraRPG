package com.borarpg.domain.usecase.quest

import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.repository.PlayerRepository

class CreateQuestUseCase(
    private val repository: PlayerRepository,
    private val newId: () -> String,
) {
    suspend operator fun invoke(title: String, latitude: Double, longitude: Double) {
        val quest = QuestPoint(newId(), title.trim(), latitude, longitude, QuestKind.EXPLORATION)
        repository.addQuest(quest)
    }
}
