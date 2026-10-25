package com.borarpg.feature.backpack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.AdventureRepository
import com.borarpg.domain.Hero
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class BackpackViewModel @Inject constructor(repository: AdventureRepository) : ViewModel() {
    val hero = repository.hero.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Hero())
}
