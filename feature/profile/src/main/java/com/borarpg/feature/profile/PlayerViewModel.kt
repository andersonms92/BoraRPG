package com.borarpg.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.AdventureRepository
import com.borarpg.data.PlayerRepository
import com.borarpg.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

@HiltViewModel
class PlayerViewModel
@Inject
constructor(private val repository: PlayerRepository, adventure: AdventureRepository) :
    ViewModel() {
    val hero = adventure.hero.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Hero())
    private val _profile = MutableStateFlow(ProfileState())
    val profile = _profile.asStateFlow()

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
}
