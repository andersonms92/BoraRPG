package com.borarpg.tracking.di

import com.borarpg.domain.tracking.WorkoutTimeSource
import com.borarpg.domain.tracking.WorkoutTracker
import com.borarpg.tracking.controller.TrackingController
import com.borarpg.tracking.time.AndroidWorkoutTimeSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {
    @Binds abstract fun tracker(controller: TrackingController): WorkoutTracker

    @Binds abstract fun time(source: AndroidWorkoutTimeSource): WorkoutTimeSource
}
