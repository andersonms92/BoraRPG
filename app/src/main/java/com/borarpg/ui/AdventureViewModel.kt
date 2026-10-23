package com.borarpg.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.AdventureRepository
import com.borarpg.data.TrackingController
import com.borarpg.data.TrackingState
import com.borarpg.domain.Hero
import kotlinx.coroutines.flow.*

data class AdventureState(
    val hero: Hero = Hero(),
    val loading: Boolean = true,
    val error: String? = null,
    val tracking: TrackingState = TrackingState(),
    val sensorAvailable: Boolean = false,
)

class AdventureViewModel(repository: AdventureRepository, private val tracker: TrackingController) :
    ViewModel() {
    val state =
        combine(repository.hero, tracker.state) { hero, tracking ->
                AdventureState(
                    hero = hero,
                    loading = false,
                    tracking = tracking,
                    sensorAvailable = tracker.available,
                )
            }
            .catch {
                emit(
                    AdventureState(
                        loading = false,
                        error = "Não foi possível carregar o progresso.",
                    )
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdventureState())

    fun startActivity() = tracker.start()

    fun stopActivity() = tracker.stop()
}
