package com.borarpg.domain

import kotlinx.coroutines.flow.StateFlow

enum class TrackingPhase {
    IDLE,
    RUNNING,
    PAUSED,
}

data class TrackingState(
    val phase: TrackingPhase = TrackingPhase.IDLE,
    val sessionId: String? = null,
    val error: String? = null,
    val elapsedMillis: Long = 0,
    val location: GeoLocation? = null,
) {
    val active: Boolean
        get() = phase != TrackingPhase.IDLE

    val paused: Boolean
        get() = phase == TrackingPhase.PAUSED
}

interface WorkoutTracker {
    val state: StateFlow<TrackingState>

    fun start(type: WorkoutType)

    fun pause()

    fun resume()

    fun finish()

    fun cancel()
}

interface WorkoutTimeSource {
    fun monotonicMillis(): Long

    fun epochMillis(): Long
}
