package com.borarpg.feature.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.AdventureRepository
import com.borarpg.domain.Hero
import com.borarpg.domain.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

data class AdventureState(
    val hero: Hero = Hero(),
    val loading: Boolean = true,
    val error: String? = null,
)

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
