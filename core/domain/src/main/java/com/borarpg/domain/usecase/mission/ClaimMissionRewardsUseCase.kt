package com.borarpg.domain.usecase.mission

import com.borarpg.domain.repository.MissionRepository

class ClaimMissionRewardsUseCase(private val repository: MissionRepository) {
    suspend operator fun invoke() = repository.claim()
}
