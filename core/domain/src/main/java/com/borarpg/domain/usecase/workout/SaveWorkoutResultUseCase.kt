package com.borarpg.domain.usecase.workout

import com.borarpg.domain.model.workout.WorkoutMutation
import com.borarpg.domain.repository.WorkoutRepository

class SaveWorkoutResultUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(id: String): WorkoutMutation = repository.saveResult(id)
}
