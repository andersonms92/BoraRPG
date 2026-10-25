package com.borarpg.domain.repository

import com.borarpg.domain.model.location.GpsState
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun updates(): Flow<GpsState>
}
