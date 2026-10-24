package com.borarpg.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.CepLookupException
import com.borarpg.data.CepRepository
import com.borarpg.domain.CepLocation
import com.borarpg.domain.QuestGenerator
import com.borarpg.domain.QuestPoint
import com.borarpg.domain.normalizeCep
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CepState(
    val input: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val target: CepLocation? = null,
    val revision: Int = 0,
    val objectives: List<QuestPoint> = emptyList(),
    val objectivesLoading: Boolean = false,
    val objectivesMessage: String? = null,
)

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
                mutable.update { it.copy(loading = false, error = e.message) }
            } catch (_: Exception) {
                mutable.update {
                    it.copy(
                        loading = false,
                        error =
                            "Não foi possível consultar o CEP. Verifique sua conexão e tente novamente.",
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
        if (!mutable.value.objectivesLoading) generateObjectives(target)
    }

    private fun generateObjectives(target: CepLocation) {
        val objectives = QuestGenerator.generate(target)
        mutable.update {
            it.copy(
                objectives = objectives,
                objectivesLoading = false,
                objectivesMessage =
                    "${objectives.size} encontros virtuais em até 1 km do CEP. Aceite uma missão para ganhar XP, ouro e itens.",
            )
        }
    }
}
