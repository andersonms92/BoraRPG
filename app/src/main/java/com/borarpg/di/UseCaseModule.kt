package com.borarpg.di

import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.repository.CepRepository
import com.borarpg.domain.repository.LocationRepository
import com.borarpg.domain.repository.MissionRepository
import com.borarpg.domain.repository.PlayerRepository
import com.borarpg.domain.repository.WorkoutRepository
import com.borarpg.domain.usecase.hero.ObserveHeroUseCase
import com.borarpg.domain.usecase.location.FindCepUseCase
import com.borarpg.domain.usecase.location.ObserveLocationsUseCase
import com.borarpg.domain.usecase.mission.AcceptMissionUseCase
import com.borarpg.domain.usecase.mission.CancelMissionUseCase
import com.borarpg.domain.usecase.mission.ClaimMissionRewardsUseCase
import com.borarpg.domain.usecase.mission.ObserveMissionsUseCase
import com.borarpg.domain.usecase.player.ObservePlayerProfileUseCase
import com.borarpg.domain.usecase.player.SavePlayerProfileUseCase
import com.borarpg.domain.usecase.quest.CreateQuestUseCase
import com.borarpg.domain.usecase.quest.DeleteQuestUseCase
import com.borarpg.domain.usecase.quest.GenerateObjectivesUseCase
import com.borarpg.domain.usecase.quest.ObserveSavedQuestsUseCase
import com.borarpg.domain.usecase.quest.SaveQuestUseCase
import com.borarpg.domain.usecase.workout.DiscardWorkoutResultUseCase
import com.borarpg.domain.usecase.workout.ObserveWorkoutRouteUseCase
import com.borarpg.domain.usecase.workout.ObserveWorkoutsUseCase
import com.borarpg.domain.usecase.workout.RecoverInterruptedWorkoutsUseCase
import com.borarpg.domain.usecase.workout.SaveWorkoutResultUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.UUID

@Module
@InstallIn(SingletonComponent::class)
internal object UseCaseModule {
    @Provides
    fun observeHero(repository: AdventureRepository): ObserveHeroUseCase =
        ObserveHeroUseCase(repository)

    @Provides
    fun observePlayerProfile(repository: PlayerRepository): ObservePlayerProfileUseCase =
        ObservePlayerProfileUseCase(repository)

    @Provides
    fun observeSavedQuests(repository: PlayerRepository): ObserveSavedQuestsUseCase =
        ObserveSavedQuestsUseCase(repository)

    @Provides
    fun saveQuest(repository: PlayerRepository): SaveQuestUseCase =
        SaveQuestUseCase(repository)

    @Provides
    fun deleteQuest(repository: PlayerRepository): DeleteQuestUseCase =
        DeleteQuestUseCase(repository)

    @Provides
    fun observeLocations(repository: LocationRepository): ObserveLocationsUseCase =
        ObserveLocationsUseCase(repository)

    @Provides
    fun claimMissionRewards(repository: MissionRepository): ClaimMissionRewardsUseCase =
        ClaimMissionRewardsUseCase(repository)

    @Provides
    fun cancelMission(repository: MissionRepository): CancelMissionUseCase =
        CancelMissionUseCase(repository)

    @Provides
    fun observeWorkouts(repository: WorkoutRepository): ObserveWorkoutsUseCase =
        ObserveWorkoutsUseCase(repository)

    @Provides
    fun observeWorkoutRoute(repository: WorkoutRepository): ObserveWorkoutRouteUseCase =
        ObserveWorkoutRouteUseCase(repository)

    @Provides
    fun saveWorkoutResult(repository: WorkoutRepository): SaveWorkoutResultUseCase =
        SaveWorkoutResultUseCase(repository)

    @Provides
    fun discardWorkoutResult(repository: WorkoutRepository): DiscardWorkoutResultUseCase =
        DiscardWorkoutResultUseCase(repository)

    @Provides
    fun recoverInterruptedWorkouts(repository: WorkoutRepository): RecoverInterruptedWorkoutsUseCase =
        RecoverInterruptedWorkoutsUseCase(repository)

    @Provides
    fun observeMissions(repository: MissionRepository): ObserveMissionsUseCase =
        ObserveMissionsUseCase(repository)

    @Provides
    fun acceptMission(repository: MissionRepository): AcceptMissionUseCase =
        AcceptMissionUseCase(repository)

    @Provides
    fun savePlayerProfile(repository: PlayerRepository): SavePlayerProfileUseCase =
        SavePlayerProfileUseCase(repository)

    @Provides
    fun createQuest(repository: PlayerRepository): CreateQuestUseCase =
        CreateQuestUseCase(repository) { UUID.randomUUID().toString() }

    @Provides
    fun generateObjectives(): GenerateObjectivesUseCase =
        GenerateObjectivesUseCase()

    @Provides
    fun findCep(repository: CepRepository): FindCepUseCase =
        FindCepUseCase(repository)
}
