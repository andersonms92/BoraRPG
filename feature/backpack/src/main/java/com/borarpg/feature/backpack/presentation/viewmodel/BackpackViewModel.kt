package com.borarpg.feature.backpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.usecase.hero.ObserveHeroUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class BackpackViewModel @Inject constructor(observeHero: ObserveHeroUseCase) : ViewModel() {
    val hero = observeHero().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Hero())
}
