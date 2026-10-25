package com.borarpg.tracking.time

import android.os.SystemClock
import com.borarpg.domain.tracking.WorkoutTimeSource
import javax.inject.Inject

class AndroidWorkoutTimeSource @Inject constructor() : WorkoutTimeSource {
    override fun monotonicMillis() = SystemClock.elapsedRealtime()

    override fun epochMillis() = System.currentTimeMillis()
}
