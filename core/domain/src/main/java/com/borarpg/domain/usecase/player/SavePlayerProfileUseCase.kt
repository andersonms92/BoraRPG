package com.borarpg.domain.usecase.player

import com.borarpg.domain.model.player.PlayerProfile
import com.borarpg.domain.repository.PlayerRepository

class SavePlayerProfileUseCase(private val repository: PlayerRepository) {
    suspend operator fun invoke(profile: PlayerProfile) {
        require(profile.complete)
        repository.save(profile.copy(name = profile.name.trim()))
    }
}
