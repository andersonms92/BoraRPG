package com.borarpg.tracking

import android.os.SystemClock
import com.borarpg.domain.WorkoutTimeSource
import com.borarpg.domain.WorkoutTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class AndroidWorkoutTimeSource @Inject constructor() : WorkoutTimeSource {
    override fun monotonicMillis() = SystemClock.elapsedRealtime()

    override fun epochMillis() = System.currentTimeMillis()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {
    @Binds abstract fun tracker(controller: TrackingController): WorkoutTracker

    @Binds abstract fun time(source: AndroidWorkoutTimeSource): WorkoutTimeSource
}
