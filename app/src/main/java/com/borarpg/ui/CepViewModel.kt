package com.borarpg.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borarpg.data.CepLookupException
import com.borarpg.data.CepRepository
import com.borarpg.data.NearbyPlacesRepository
import com.borarpg.domain.CepLocation
import com.borarpg.domain.QuestGenerator
import com.borarpg.domain.QuestPoint
import com.borarpg.domain.normalizeCep
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

class CepViewModel : ViewModel() {
    private val repository = CepRepository()
    private val places = NearbyPlacesRepository()
    private val mutable = MutableStateFlow(CepState())
    val state = mutable.asStateFlow()
    private var request: Job? = null
    private var objectiveRequest: Job? = null

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
                objectiveRequest?.cancel()
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
        objectiveRequest = viewModelScope.launch {
            mutable.update { it.copy(objectivesLoading = true, objectivesMessage = null) }
            try {
                val objectives = QuestGenerator.generate(target, places.find(target))
                mutable.update {
                    it.copy(
                        objectives = objectives,
                        objectivesLoading = false,
                        objectivesMessage =
                            if (objectives.isEmpty())
                                "Nenhuma praça ou parque próximo foi encontrado no OpenStreetMap. Você pode marcar seus próprios locais."
                            else "${objectives.size} objetivos em até 2 km do CEP · OpenStreetMap",
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutable.update {
                    it.copy(
                        objectivesLoading = false,
                        objectivesMessage =
                            "Não foi possível buscar locais próximos. Tente gerar os objetivos novamente.",
                    )
                }
            }
        }
    }
}
