package com.borarpg.feature.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.repository.PlayerRepository
import com.borarpg.feature.profile.presentation.form.ProfileForm
import com.borarpg.feature.profile.presentation.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                _profile.value =
                    ProfileState(
                        draft = ProfileForm.from(repository.profile.first()),
                        loading = false,
                    )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profile.value =
                    ProfileState(loading = false, message = "Não foi possível carregar o perfil.")
            }
        }
    }

    fun edit(profile: ProfileForm) {
        _profile.update { it.copy(draft = profile, errors = emptyMap(), message = null) }
    }

    fun save() {
        if (_profile.value.saving || _profile.value.loading) return
        val draft = _profile.value.draft
        val errors = draft.errors()
        if (errors.isNotEmpty()) {
            _profile.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _profile.update { it.copy(saving = true) }
            try {
                repository.save(draft.toDomain())
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
