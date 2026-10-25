package com.borarpg.feature.backpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.repository.AdventureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class BackpackViewModel @Inject constructor(repository: AdventureRepository) : ViewModel() {
    val hero = repository.hero.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Hero())
}
