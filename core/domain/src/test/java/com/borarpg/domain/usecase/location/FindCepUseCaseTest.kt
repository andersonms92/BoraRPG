package com.borarpg.domain.usecase.location

import com.borarpg.domain.error.CepLookupError
import com.borarpg.domain.error.CepLookupException
import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.repository.CepRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FindCepUseCaseTest {
    @Test
    fun normalizesFormattedCepAndPreservesLeadingZeroBeforeLookup() = runTest {
        val repository = RecordingCepRepository()
        val lookup = FindCepUseCase(repository)
        val result = lookup(" 01310-100 ")
        assertEquals(listOf("01310100"), repository.queries)
        assertSame(repository.location, result)
    }

    @Test
    fun malformedInputNeverReachesRepository() = runTest {
        val repository = RecordingCepRepository()
        val lookup = FindCepUseCase(repository)
        for (input in listOf("", "0131010", "013101000", "01310a100", "0131-0100", "01310 100")) {
            val error = runCatching { lookup(input) }.exceptionOrNull()
            assertTrue(error is CepLookupException)
            assertEquals(CepLookupError.INVALID_CEP, (error as CepLookupException).error)
            assertNull(error.cause)
        }
        assertTrue(repository.queries.isEmpty())
    }

    @Test
    fun cancellationFromLookupPropagatesWithoutBecomingDomainFailure() = runTest {
        val cancellation = CancellationException("Lookup cancelled")
        val repository = RecordingCepRepository(cancellation)
        val error = runCatching { FindCepUseCase(repository)("01310100") }.exceptionOrNull()
        assertSame(cancellation, error)
        assertEquals(listOf("01310100"), repository.queries)
    }

    @Test
    fun preservesRepositoryDomainFailureAndOriginalCause() = runTest {
        val cause = IllegalStateException("Remote lookup unavailable")
        val failure = CepLookupException(CepLookupError.SERVICE_UNAVAILABLE, cause)
        val repository = RecordingCepRepository(failure)

        val error = runCatching { FindCepUseCase(repository)("01310100") }.exceptionOrNull()

        assertSame(failure, error)
        assertSame(cause, error?.cause)
        assertEquals(listOf("01310100"), repository.queries)
    }

    private class RecordingCepRepository(private val failure: Exception? = null) : CepRepository {
        val queries = mutableListOf<String>()
        val location = CepLocation("01310100", "São Paulo", -23.55, -46.63)

        override suspend fun find(input: String): CepLocation {
            queries += input
            failure?.let { throw it }
            return location
        }
    }
}
