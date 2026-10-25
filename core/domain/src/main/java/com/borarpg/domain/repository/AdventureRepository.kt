package com.borarpg.domain.repository

import com.borarpg.domain.model.hero.Hero
import kotlinx.coroutines.flow.Flow

interface AdventureRepository {
    val hero: Flow<Hero>

    suspend fun recordSteps(steps: Int)
}
