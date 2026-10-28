package com.borarpg.data.mapper

import com.borarpg.domain.error.CepLookupError
import com.borarpg.domain.error.CepLookupException
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CepErrorMapperTest {
    @Test
    fun `cancelamento nao se transforma em falha de consulta`() {
        val cancellation = CancellationException("Consulta cancelada")

        val thrown = runCatching { cancellation.toCepLookupException() }.exceptionOrNull()

        assertSame(cancellation, thrown)
    }

    @Test
    fun `falha de dominio existente preserva identidade e causa`() {
        val cause = IllegalArgumentException("Coordenadas invalidas")
        val failure = CepLookupException(CepLookupError.LOCATION_UNAVAILABLE, cause)

        val mapped = failure.toCepLookupException()

        assertSame(failure, mapped)
        assertSame(cause, mapped.cause)
    }

    @Test
    fun `erro inesperado nao se transforma em falha de conexao`() {
        val failure = IllegalStateException("Configuracao invalida")

        val thrown = runCatching { failure.toCepLookupException() }.exceptionOrNull()

        assertSame(failure, thrown)
    }

    @Test
    fun `JSON malformado e resposta invalida mesmo sendo falha de IO`() {
        val failure = JsonEncodingException("JSON invalido")

        val mapped = failure.toCepLookupException()

        assertEquals(CepLookupError.INVALID_RESPONSE, mapped.error)
        assertSame(failure, mapped.cause)
    }
}
