package com.borarpg.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.AdventureRepository
import com.borarpg.domain.Hero
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdventureState(
    val hero: Hero = Hero(),
    val loading: Boolean = true,
    val error: String? = null,
)

class AdventureViewModel(private val repository: AdventureRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AdventureState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.hero
                .catch {
                    mutableState.value =
                        AdventureState(
                            loading = false,
                            error = "Não foi possível carregar o progresso.",
                        )
                }
                .collect { mutableState.value = AdventureState(hero = it, loading = false) }
        }
    }

    fun simulate() {
        viewModelScope.launch {
            try {
                repository.simulateSteps(250)
            } catch (_: Exception) {
                mutableState.update { it.copy(error = "Não foi possível salvar. Tente novamente.") }
            }
        }
    }
}
