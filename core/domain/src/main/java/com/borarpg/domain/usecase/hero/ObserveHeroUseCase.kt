package com.borarpg.domain.usecase.hero

import com.borarpg.domain.model.hero.Hero
import kotlinx.coroutines.flow.Flow
import com.borarpg.domain.repository.AdventureRepository

class ObserveHeroUseCase(private val repository: AdventureRepository) {
    operator fun invoke(): Flow<Hero> = repository.hero
}
