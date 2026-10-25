package com.borarpg.feature.journey.presentation.state

import com.borarpg.domain.model.hero.Hero

data class AdventureState(
    val hero: Hero = Hero(),
    val loading: Boolean = true,
    val error: String? = null,
)
