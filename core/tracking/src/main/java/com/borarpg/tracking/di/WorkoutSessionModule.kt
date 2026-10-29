package com.borarpg.tracking.di

import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.repository.PlayerRepository
import com.borarpg.domain.repository.WorkoutRepository
import com.borarpg.domain.rules.workout.WorkoutSessionManager
import com.borarpg.domain.tracking.WorkoutTimeSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import java.util.UUID

@Module
@InstallIn(ServiceComponent::class)
internal object WorkoutSessionModule {
    @Provides
    @ServiceScoped
    fun sessionManager(
        repository: WorkoutRepository,
        player: PlayerRepository,
        adventure: AdventureRepository,
        time: WorkoutTimeSource,
    ): WorkoutSessionManager =
        WorkoutSessionManager(repository, player, adventure, time) { UUID.randomUUID().toString() }
}
