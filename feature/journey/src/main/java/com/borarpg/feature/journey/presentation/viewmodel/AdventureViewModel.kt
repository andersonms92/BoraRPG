package com.borarpg.feature.journey.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.repository.PlayerRepository
import com.borarpg.feature.journey.presentation.state.AdventureState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AdventureViewModel
@Inject
constructor(repository: AdventureRepository, player: PlayerRepository) : ViewModel() {
    val name =
        player.profile
            .map { it.name }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val state =
        repository.hero
            .map { AdventureState(hero = it, loading = false) }
            .catch {
                emit(
                    AdventureState(
                        loading = false,
                        error = "Não foi possível carregar o progresso.",
                    )
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdventureState())
}
