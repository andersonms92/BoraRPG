package com.borarpg.domain.usecase.workout

import com.borarpg.domain.model.workout.WorkoutSession
import kotlinx.coroutines.flow.Flow
import com.borarpg.domain.repository.WorkoutRepository

class ObserveWorkoutsUseCase(private val repository: WorkoutRepository) {
    operator fun invoke(): Flow<List<WorkoutSession>> = repository.workouts
}
