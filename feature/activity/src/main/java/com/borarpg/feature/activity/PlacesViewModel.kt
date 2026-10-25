package com.borarpg.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.LocationRepository
import com.borarpg.domain.PlayerRepository
import com.borarpg.domain.QuestPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class PlacesViewModel
@Inject
constructor(private val repository: PlayerRepository, locationRepository: LocationRepository) :
    ViewModel() {
    val locations = locationRepository.updates()
    private val mutableMessage = MutableStateFlow<String?>(null)
    val message = mutableMessage.asStateFlow()
    val quests =
        repository.quests
            .catch {
                mutableMessage.value = "Não foi possível carregar os locais."
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addQuest(quest: QuestPoint) = update { repository.addQuest(quest) }

    fun deleteQuest(id: String) = update { repository.deleteQuest(id) }

    private fun update(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                mutableMessage.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableMessage.value = "Não foi possível atualizar o local."
            }
        }
    }
}
