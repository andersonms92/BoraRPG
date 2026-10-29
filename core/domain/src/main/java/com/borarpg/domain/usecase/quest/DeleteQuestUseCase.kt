package com.borarpg.domain.usecase.quest

import com.borarpg.domain.repository.PlayerRepository

class DeleteQuestUseCase(private val repository: PlayerRepository) {
    suspend operator fun invoke(id: String) = repository.deleteQuest(id)
}
