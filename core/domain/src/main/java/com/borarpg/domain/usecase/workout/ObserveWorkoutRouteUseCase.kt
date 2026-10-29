package com.borarpg.domain.usecase.workout

import com.borarpg.domain.model.workout.RoutePoint
import kotlinx.coroutines.flow.Flow
import com.borarpg.domain.repository.WorkoutRepository

class ObserveWorkoutRouteUseCase(private val repository: WorkoutRepository) {
    operator fun invoke(id: String): Flow<List<RoutePoint>> = repository.observeRoute(id)
}
