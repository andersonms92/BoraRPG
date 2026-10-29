package com.borarpg.domain.usecase.mission

import com.borarpg.domain.model.mission.MissionOverview
import com.borarpg.domain.repository.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveMissionsUseCase(private val repository: MissionRepository) {
    operator fun invoke(): Flow<MissionOverview> =
        combine(repository.active, repository.inventory, repository.claimed, ::MissionOverview)
}
