package com.borarpg.domain

import kotlinx.coroutines.flow.Flow

interface AdventureRepository {
    val hero: Flow<Hero>

    suspend fun recordSteps(steps: Int)
}
