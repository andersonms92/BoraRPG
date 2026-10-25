package com.borarpg.tracking.service

import com.borarpg.domain.model.location.GeoLocation
import com.borarpg.domain.model.workout.WorkoutType

internal sealed interface TrackingCommand {
    data class Start(val type: WorkoutType) : TrackingCommand

    data object Resume : TrackingCommand

    data object Pause : TrackingCommand

    data object Finish : TrackingCommand

    data object Cancel : TrackingCommand

    data object LocationUnavailable : TrackingCommand

    data object Tick : TrackingCommand

    data class Position(val location: GeoLocation, val timestamp: Long) : TrackingCommand

    data class Steps(val count: Long) : TrackingCommand
}
