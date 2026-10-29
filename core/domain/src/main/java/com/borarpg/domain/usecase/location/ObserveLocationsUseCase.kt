package com.borarpg.domain.usecase.location

import com.borarpg.domain.model.location.GpsState
import kotlinx.coroutines.flow.Flow
import com.borarpg.domain.repository.LocationRepository

class ObserveLocationsUseCase(private val repository: LocationRepository) {
    operator fun invoke(): Flow<GpsState> = repository.updates()
}
