package com.borarpg.domain.model.workout

import com.borarpg.domain.model.location.GeoLocation

data class TrackingState(
    val phase: TrackingPhase = TrackingPhase.IDLE,
    val sessionId: String? = null,
    val error: String? = null,
    val elapsedMillis: Long = 0,
    val location: GeoLocation? = null,
) {
    val active: Boolean
        get() = phase != TrackingPhase.IDLE
}
