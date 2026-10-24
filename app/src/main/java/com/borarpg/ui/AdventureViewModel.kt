package com.borarpg.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.AdventureRepository
import com.borarpg.domain.Hero
import kotlinx.coroutines.flow.*

data class AdventureState(
    val hero: Hero = Hero(),
    val loading: Boolean = true,
    val error: String? = null,
)

class AdventureViewModel(repository: AdventureRepository) : ViewModel() {
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
