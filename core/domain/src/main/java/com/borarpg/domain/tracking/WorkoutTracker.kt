package com.borarpg.domain.tracking

import com.borarpg.domain.model.workout.TrackingState
import com.borarpg.domain.model.workout.WorkoutType
import kotlinx.coroutines.flow.StateFlow

interface WorkoutTracker {
    val state: StateFlow<TrackingState>

    fun start(type: WorkoutType)

    fun pause()

    fun resume()

    fun finish()

    fun cancel()
}
