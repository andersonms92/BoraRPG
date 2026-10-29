package com.borarpg.domain.usecase.quest

import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.repository.PlayerRepository

class SaveQuestUseCase(private val repository: PlayerRepository) {
    suspend operator fun invoke(quest: QuestPoint) = repository.addQuest(quest)
}
