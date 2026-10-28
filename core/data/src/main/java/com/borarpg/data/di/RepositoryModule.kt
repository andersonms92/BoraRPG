package com.borarpg.data.di

import com.borarpg.data.location.AndroidLocationRepository
import com.borarpg.data.repository.DataStorePlayerRepository
import com.borarpg.data.repository.CepRepositoryImpl
import com.borarpg.data.repository.RoomAdventureRepository
import com.borarpg.data.repository.RoomMissionRepository
import com.borarpg.data.repository.RoomWorkoutRepository
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
    @Binds abstract fun adventure(repository: RoomAdventureRepository): AdventureRepository

    @Binds abstract fun player(repository: DataStorePlayerRepository): PlayerRepository

    @Binds abstract fun missions(repository: RoomMissionRepository): MissionRepository

    @Binds abstract fun cep(repository: CepRepositoryImpl): CepRepository

    @Binds abstract fun location(repository: AndroidLocationRepository): LocationRepository

    @Binds abstract fun workouts(repository: RoomWorkoutRepository): WorkoutRepository
}
