package com.borarpg.domain.usecase.mission

import com.borarpg.domain.repository.MissionRepository

class CancelMissionUseCase(private val repository: MissionRepository) {
    suspend operator fun invoke() = repository.cancel()
}
