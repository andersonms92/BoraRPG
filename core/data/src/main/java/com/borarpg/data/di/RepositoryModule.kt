package com.borarpg.data.di

import com.borarpg.data.*
import com.borarpg.domain.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun adventure(repository: RoomAdventureRepository): AdventureRepository

    @Binds abstract fun player(repository: DataStorePlayerRepository): PlayerRepository

    @Binds abstract fun missions(repository: RoomMissionRepository): MissionRepository

    @Binds abstract fun cep(repository: HttpCepRepository): CepRepository

    @Binds abstract fun location(repository: AndroidLocationRepository): LocationRepository

    @Binds abstract fun workouts(repository: RoomWorkoutRepository): WorkoutRepository
}
