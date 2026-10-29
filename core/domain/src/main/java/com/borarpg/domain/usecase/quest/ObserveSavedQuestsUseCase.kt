package com.borarpg.domain.usecase.quest

import com.borarpg.domain.model.mission.QuestPoint
import kotlinx.coroutines.flow.Flow
import com.borarpg.domain.repository.PlayerRepository

class ObserveSavedQuestsUseCase(private val repository: PlayerRepository) {
    operator fun invoke(): Flow<List<QuestPoint>> = repository.quests
}
