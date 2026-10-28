package com.borarpg.data.repository

import com.borarpg.data.remote.cep.CepApi
import com.borarpg.data.remote.cep.CepRemoteDataSource
import com.borarpg.domain.error.CepLookupError
import com.borarpg.domain.error.CepLookupException
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CepRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var repository: CepRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        client = OkHttpClient.Builder().retryOnConnectionFailure(false).build()
        val api =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
                .build()
                .create(CepApi::class.java)
        repository = CepRepositoryImpl(CepRemoteDataSource(api))
    }

    @After
    fun tearDown() {
        client.dispatcher.cancelAll()
        client.connectionPool.evictAll()
        server.shutdown()
        client.dispatcher.executorService.shutdown()
    }

    @Test
    fun `consulta CEP normalizado e mapeia coordenadas em texto`() = runBlocking {
        server.enqueue(jsonResponse(addressJson()))

        val location = repository.find("01310-100")

        assertEquals("01310-100", location.cep)
        assertEquals("Avenida Paulista · Bela Vista · São Paulo · SP", location.address)
        assertEquals(-23.5614, location.latitude, 0.000001)
        assertEquals(-46.6559, location.longitude, 0.000001)
        val request = server.takeRequest(2, TimeUnit.SECONDS)
        assertNotNull(request)
        assertEquals("/api/cep/v2/01310100", request?.path)
        assertEquals("application/json", request?.getHeader("Accept"))
    }

    @Test
    fun `aceita coordenadas numericas e campos opcionais ausentes`() = runBlocking {
        server.enqueue(
            jsonResponse(
                """{"location":{"coordinates":{"latitude":-23.5614,"longitude":-46.6559}}}"""
            )
        )

        val location = repository.find("01310100")

        assertEquals("CEP 01310100", location.address)
        assertEquals(-23.5614, location.latitude, 0.000001)
        assertEquals(-46.6559, location.longitude, 0.000001)
    }

    @Test
    fun `ignora campos de endereco nulos`() = runBlocking {
        server.enqueue(jsonResponse(addressJson().replace("\"Bela Vista\"", "null")))

        val location = repository.find("01310100")

        assertEquals("Avenida Paulista · São Paulo · SP", location.address)
    }

    @Test
    fun `rejeita CEP invalido sem fazer requisicao`() = runBlocking {
        val error = lookupFailure { repository.find("123") }

        assertEquals(CepLookupError.INVALID_CEP, error.error)
        assertNull(error.cause)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `mapeia HTTP 400 e 404 para CEP nao encontrado`() = runBlocking {
        for (status in listOf(400, 404)) {
            server.enqueue(MockResponse().setResponseCode(status).setBody("{}"))

            val error = lookupFailure { repository.find("01310100") }

            assertEquals(CepLookupError.NOT_FOUND, error.error)
            assertEquals(status, (error.cause as HttpException).code())
        }
    }

    @Test
    fun `mapeia falha do servidor para indisponibilidade`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503).setBody("{}"))

        val error = lookupFailure { repository.find("01310100") }

        assertEquals(CepLookupError.SERVICE_UNAVAILABLE, error.error)
        assertEquals(503, (error.cause as HttpException).code())
    }

    @Test
    fun `mapeia interrupcao de conexao para erro de dominio`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val error = lookupFailure { repository.find("01310100") }

        assertEquals(CepLookupError.CONNECTION, error.error)
        assertTrue(error.cause is IOException)
    }

    @Test
    fun `mapeia estrutura JSON invalida para erro de dominio`() = runBlocking {
        server.enqueue(jsonResponse("""{"location":[]}"""))

        val error = lookupFailure { repository.find("01310100") }

        assertEquals(CepLookupError.INVALID_RESPONSE, error.error)
        assertTrue(error.cause is JsonDataException)
    }

    @Test
    fun `mapeia JSON malformado para erro de dominio`() = runBlocking {
        server.enqueue(jsonResponse("{invalido"))

        val error = lookupFailure { repository.find("01310100") }

        assertEquals(CepLookupError.INVALID_RESPONSE, error.error)
        assertTrue(error.cause is JsonEncodingException)
    }

    @Test
    fun `rejeita resposta sem corpo`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))

        val error = lookupFailure { repository.find("01310100") }

        assertEquals(CepLookupError.INVALID_RESPONSE, error.error)
        assertNull(error.cause)
    }

    @Test
    fun `rejeita localizacao ou coordenadas ausentes e invalidas`() = runBlocking {
        val responses =
            listOf(
                "{}",
                """{"location":null}""",
                """{"location":{"coordinates":null}}""",
                """{"location":{"coordinates":{"latitude":null,"longitude":"-46.6559"}}}""",
                addressJson(latitude = "invalida"),
                addressJson(latitude = "NaN"),
                addressJson(longitude = "Infinity"),
                addressJson(latitude = "91"),
                addressJson(longitude = "181"),
                addressJson(latitude = "0", longitude = "0"),
            )
        for (body in responses) {
            server.enqueue(jsonResponse(body))

            val error = lookupFailure { repository.find("01310100") }

            assertEquals(CepLookupError.LOCATION_UNAVAILABLE, error.error)
            assertNull(error.cause)
        }
    }

    @Test
    fun `preserva cancelamento da coroutine durante requisicao`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val request = async { repository.find("01310100") }
        assertNotNull(withContext(Dispatchers.IO) { server.takeRequest(2, TimeUnit.SECONDS) })

        request.cancel()

        withTimeout(2000) {
            try {
                request.await()
                throw AssertionError("O cancelamento deveria interromper a consulta.")
            } catch (_: CancellationException) {
                assertTrue(request.isCancelled)
            }
        }
    }

    private suspend fun lookupFailure(operation: suspend () -> Unit): CepLookupException {
        try {
            operation()
        } catch (error: CepLookupException) {
            return error
        }
        throw AssertionError("A consulta deveria falhar com CepLookupException.")
    }

    private fun jsonResponse(body: String): MockResponse =
        MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private fun addressJson(
        latitude: String = "-23.5614",
        longitude: String = "-46.6559",
    ): String =
        """
        {
          "cep": "01310100",
          "state": "SP",
          "city": "São Paulo",
          "neighborhood": "Bela Vista",
          "street": "Avenida Paulista",
          "service": "open-cep",
          "location": {
            "type": "Point",
            "coordinates": {"latitude": "$latitude", "longitude": "$longitude"}
          }
        }
        """.trimIndent()
}
