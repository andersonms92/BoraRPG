package com.borarpg.domain.usecase.player

import com.borarpg.domain.model.player.PlayerProfile
import kotlinx.coroutines.flow.Flow
import com.borarpg.domain.repository.PlayerRepository

class ObservePlayerProfileUseCase(private val repository: PlayerRepository) {
    operator fun invoke(): Flow<PlayerProfile> = repository.profile
}
