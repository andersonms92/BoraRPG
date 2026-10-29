package com.borarpg.domain.usecase.workout

import com.borarpg.domain.model.workout.WorkoutMutation
import com.borarpg.domain.repository.WorkoutRepository

class DiscardWorkoutResultUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(id: String): WorkoutMutation = repository.discard(id)
}
