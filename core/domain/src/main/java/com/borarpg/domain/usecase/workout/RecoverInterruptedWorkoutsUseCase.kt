package com.borarpg.domain.usecase.workout

import com.borarpg.domain.repository.WorkoutRepository

class RecoverInterruptedWorkoutsUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke() = repository.recoverInterruptedSessions()
}
