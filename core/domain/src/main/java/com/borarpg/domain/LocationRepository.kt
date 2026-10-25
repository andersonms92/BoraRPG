package com.borarpg.domain

import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun updates(): Flow<GpsState>
}
