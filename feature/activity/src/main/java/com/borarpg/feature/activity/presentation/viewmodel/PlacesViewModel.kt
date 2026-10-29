package com.borarpg.feature.activity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.usecase.location.ObserveLocationsUseCase
import com.borarpg.domain.usecase.quest.CreateQuestUseCase
import com.borarpg.domain.usecase.quest.DeleteQuestUseCase
import com.borarpg.domain.usecase.quest.ObserveSavedQuestsUseCase
import com.borarpg.domain.usecase.quest.SaveQuestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
constructor(
    observeSavedQuests: ObserveSavedQuestsUseCase,
    observeLocations: ObserveLocationsUseCase,
    private val createQuestUseCase: CreateQuestUseCase,
    private val saveQuest: SaveQuestUseCase,
    private val deleteSavedQuest: DeleteQuestUseCase,
) : ViewModel() {
    val locations = observeLocations()
    private val mutableMessage = MutableStateFlow<String?>(null)
    val message = mutableMessage.asStateFlow()
    val quests =
        observeSavedQuests()
            .catch {
                mutableMessage.value = "Não foi possível carregar os locais."
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createQuest(title: String, latitude: Double, longitude: Double) = update {
        createQuestUseCase(title, latitude, longitude)
    }

    fun addQuest(quest: QuestPoint) = update { saveQuest(quest) }

    fun deleteQuest(id: String) = update { deleteSavedQuest(id) }

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
