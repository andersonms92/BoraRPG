package com.borarpg.data.di

import com.borarpg.data.location.LocationRepositoryImpl
import com.borarpg.data.repository.AdventureRepositoryImpl
import com.borarpg.data.repository.CepRepositoryImpl
import com.borarpg.data.repository.MissionRepositoryImpl
import com.borarpg.data.repository.PlayerRepositoryImpl
import com.borarpg.data.repository.WorkoutRepositoryImpl
import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.repository.CepRepository
import com.borarpg.domain.repository.LocationRepository
import com.borarpg.domain.repository.MissionRepository
import com.borarpg.domain.repository.PlayerRepository
import com.borarpg.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds abstract fun adventure(repository: AdventureRepositoryImpl): AdventureRepository

    @Binds abstract fun player(repository: PlayerRepositoryImpl): PlayerRepository

    @Binds abstract fun missions(repository: MissionRepositoryImpl): MissionRepository

    @Binds abstract fun cep(repository: CepRepositoryImpl): CepRepository

    @Binds abstract fun location(repository: LocationRepositoryImpl): LocationRepository

    @Binds abstract fun workouts(repository: WorkoutRepositoryImpl): WorkoutRepository
}
