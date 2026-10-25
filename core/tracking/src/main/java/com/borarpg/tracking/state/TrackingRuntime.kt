package com.borarpg.tracking.state

import com.borarpg.domain.model.workout.TrackingState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TrackingRuntime @Inject constructor() {
    private val mutable = MutableStateFlow(TrackingState())
    val state = mutable.asStateFlow()

    fun update(state: TrackingState) {
        mutable.value = state
    }
}
