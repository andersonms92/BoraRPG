package com.borarpg.feature.activity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.repository.LocationRepository
import com.borarpg.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
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

    fun createQuest(title: String, latitude: Double, longitude: Double) = update {
        repository.addQuest(
            QuestPoint(UUID.randomUUID().toString(), title, latitude, longitude, QuestKind.EXPLORATION)
        )
    }

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
