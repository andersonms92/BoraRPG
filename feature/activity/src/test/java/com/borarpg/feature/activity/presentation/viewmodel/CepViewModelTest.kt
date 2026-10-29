package com.borarpg.feature.activity.presentation.viewmodel

import androidx.lifecycle.ViewModelStore
import com.borarpg.domain.error.CepLookupError
import com.borarpg.domain.error.CepLookupException
import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.repository.CepRepository
import com.borarpg.domain.usecase.location.FindCepUseCase
import com.borarpg.domain.usecase.quest.GenerateObjectivesUseCase
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CepViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val viewModels = ViewModelStore()
    private val repository = CepStore()

    private class CepStore : CepRepository {
        val result = CepLocation("01310100", "Avenida Paulista", -23.5614, -46.6559)
        val requests = mutableListOf<String>()
        var failure: Exception? = null
        var pending: CompletableDeferred<CepLocation>? = null
        var cancellations = 0

        override suspend fun find(input: String): CepLocation {
            requests += input
            try {
                failure?.let { throw it }
                return pending?.await() ?: result
            } catch (error: CancellationException) {
                cancellations++
                throw error
            }
        }
    }

    private fun model() =
        CepViewModel(FindCepUseCase(repository), GenerateObjectivesUseCase(Random(1))).also {
            viewModels.put("cep", it)
        }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun cleanup() {
        viewModels.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun invalidCepShowsValidationWithoutConsultingRepository() = runTest {
        val vm = model()
        vm.edit("12345")
        vm.search()
        advanceUntilIdle()

        assertTrue(repository.requests.isEmpty())
        assertEquals("Digite um CEP válido, como 01310-100.", vm.state.value.error)
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.target)
        assertTrue(vm.state.value.objectives.isEmpty())
    }

    @Test
    fun lookupFailuresReachPresentationAsActionableMessages() = runTest {
        val vm = model()
        vm.edit("01310-100")
        val failures =
            mapOf(
                CepLookupError.NOT_FOUND to
                    "CEP não encontrado. Confira os números e tente novamente.",
                CepLookupError.LOCATION_UNAVAILABLE to
                    "O CEP existe, mas não possui posição no mapa. Tente um CEP próximo ou mova o mapa manualmente.",
                CepLookupError.SERVICE_UNAVAILABLE to
                    "A busca por CEP está indisponível. Tente novamente em instantes.",
                CepLookupError.CONNECTION to
                    "Não foi possível consultar o CEP. Verifique sua conexão e tente novamente.",
                CepLookupError.INVALID_RESPONSE to
                    "A busca por CEP retornou uma resposta inválida.",
            )

        failures.forEach { (error, message) ->
            repository.failure = CepLookupException(error)
            vm.search()
            advanceUntilIdle()

            assertEquals(error.name, message, vm.state.value.error)
            assertFalse(vm.state.value.loading)
            assertNull(vm.state.value.target)
            assertTrue(vm.state.value.objectives.isEmpty())
        }
        assertEquals(failures.size, repository.requests.size)
    }

    @Test
    fun unexpectedFailureDoesNotClaimAConnectionProblemOrExposeTechnicalDetails() = runTest {
        repository.failure = IllegalStateException("internal state detail")
        val vm = model()
        vm.edit("01310-100")
        vm.search()
        advanceUntilIdle()

        assertEquals("Não foi possível concluir a busca. Tente novamente.", vm.state.value.error)
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.target)
    }

    @Test
    fun cancelPendingSearchDoesNotPublishAnErrorOrLateResultAndAllowsAnotherSearch() = runTest {
        val pending = CompletableDeferred<CepLocation>()
        repository.pending = pending
        val vm = model()
        vm.edit("01310-100")
        vm.search()
        runCurrent()
        assertTrue(vm.state.value.loading)

        vm.cancelSearch()
        runCurrent()
        pending.complete(repository.result)
        advanceUntilIdle()

        assertEquals(1, repository.cancellations)
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.error)
        assertNull(vm.state.value.target)
        assertTrue(vm.state.value.objectives.isEmpty())

        repository.pending = null
        vm.search()
        advanceUntilIdle()
        assertEquals(repository.result, vm.state.value.target)
        assertEquals(2, repository.requests.size)
    }

    @Test
    fun pendingSearchIgnoresDuplicateRequestsAndInputChanges() = runTest {
        repository.pending = CompletableDeferred()
        val vm = model()
        vm.edit("01310-100")
        vm.search()
        runCurrent()
        vm.search()
        vm.edit("20040-020")
        runCurrent()

        assertEquals(listOf("01310100"), repository.requests)
        assertEquals("01310-100", vm.state.value.input)
        assertTrue(vm.state.value.loading)
        vm.cancelSearch()
        runCurrent()
    }

    @Test
    fun retryAfterLookupFailurePublishesTargetAndGeneratedObjectives() = runTest {
        repository.failure = CepLookupException(CepLookupError.NOT_FOUND)
        val vm = model()
        vm.edit("01310-100")
        vm.search()
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        repository.failure = null
        vm.search()
        advanceUntilIdle()

        assertEquals(listOf("01310100", "01310100"), repository.requests)
        assertEquals(repository.result, vm.state.value.target)
        assertEquals(1, vm.state.value.revision)
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.error)
        assertTrue(vm.state.value.objectives.isNotEmpty())
        assertNotNull(vm.state.value.objectivesMessage)
    }
}
