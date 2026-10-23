package com.borarpg.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.PlayerRepository
import com.borarpg.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileState(
    val draft: PlayerProfile = PlayerProfile(),
    val errors: Map<String, String> = emptyMap(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val message: String? = null,
)

class PlayerViewModel(private val repository: PlayerRepository) : ViewModel() {
    private val _profile = MutableStateFlow(ProfileState())
    val profile = _profile.asStateFlow()
    val quests =
        repository.quests
            .catch {
                _profile.update { state ->
                    state.copy(message = "Não foi possível carregar os locais.")
                }
                emit(sampleQuests)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sampleQuests)

    init {
        viewModelScope.launch {
            try {
                _profile.value = ProfileState(draft = repository.profile.first(), loading = false)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profile.value =
                    ProfileState(loading = false, message = "Não foi possível carregar o perfil.")
            }
        }
    }

    fun edit(profile: PlayerProfile) {
        _profile.update { it.copy(draft = profile, errors = emptyMap(), message = null) }
    }

    fun save() {
        val draft = _profile.value.draft
        val errors = draft.errors()
        if (errors.isNotEmpty()) {
            _profile.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _profile.update { it.copy(saving = true) }
            try {
                repository.save(draft)
                _profile.update {
                    it.copy(saving = false, message = "Perfil salvo neste aparelho.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profile.update {
                    it.copy(saving = false, message = "Não foi possível salvar. Tente novamente.")
                }
            }
        }
    }

    fun addQuest(point: QuestPoint) = viewModelScope.launch {
        try {
            repository.addQuest(point)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _profile.update { it.copy(message = "Não foi possível salvar o local.") }
        }
    }

    fun deleteQuest(id: String) = viewModelScope.launch {
        try {
            repository.deleteQuest(id)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _profile.update { it.copy(message = "Não foi possível remover o local.") }
        }
    }
}
