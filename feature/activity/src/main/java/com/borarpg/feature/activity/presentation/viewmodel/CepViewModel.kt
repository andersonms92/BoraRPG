package com.borarpg.feature.activity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.domain.error.CepLookupException
import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.repository.CepRepository
import com.borarpg.domain.rules.location.normalizeCep
import com.borarpg.domain.rules.mission.QuestGenerator
import com.borarpg.feature.activity.presentation.formatter.message
import com.borarpg.feature.activity.presentation.state.CepState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CepViewModel @Inject constructor(private val repository: CepRepository) : ViewModel() {

    private val mutable = MutableStateFlow(CepState())
    val state = mutable.asStateFlow()
    private var request: Job? = null

    fun edit(value: String) {
        if (!mutable.value.loading) mutable.update { it.copy(input = value.take(9), error = null) }
    }

    fun search() {
        if (mutable.value.loading) return
        val input = mutable.value.input
        if (normalizeCep(input) == null) {
            mutable.update { it.copy(error = "Digite um CEP válido, como 01310-100.") }
            return
        }
        request = viewModelScope.launch {
            mutable.update { it.copy(loading = true, error = null) }
            try {
                val result = repository.find(input)

                mutable.update {
                    it.copy(
                        loading = false,
                        target = result,
                        revision = it.revision + 1,
                        objectives = emptyList(),
                        objectivesMessage = null,
                    )
                }
                generateObjectives(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: CepLookupException) {
                mutable.update { it.copy(loading = false, error = e.error.message) }
            } catch (_: Exception) {
                mutable.update {
                    it.copy(
                        loading = false,
                        error =
                            "Não foi possível concluir a busca. Tente novamente.",
                    )
                }
            }
        }
    }

    fun cancelSearch() {
        request?.cancel()
        mutable.update { it.copy(loading = false, error = null) }
    }

    fun clearTarget() {
        mutable.update { it.copy(target = null) }
    }

    fun regenerateObjectives() {
        val target = mutable.value.target ?: return
        generateObjectives(target)
    }

    private fun generateObjectives(target: CepLocation) {
        val objectives = QuestGenerator.generate(target)
        mutable.update {
            it.copy(
                objectives = objectives,
                objectivesMessage =
                    "${objectives.size} encontros virtuais em até 1 km do CEP. Aceite uma missão para ganhar XP, ouro e itens.",
            )
        }
    }
}
