package com.borarpg.data.remote.cep

import com.borarpg.data.di.IoDispatcher
import com.borarpg.domain.error.CepLookupException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CepRemoteDataSource
@Inject
constructor(@param:IoDispatcher private val dispatcher: CoroutineDispatcher) {
    suspend fun find(cep: String): CepResponse =
        withContext(dispatcher) {
            val connection =
                URL("https://brasilapi.com.br/api/cep/v2/$cep").openConnection()
                    as HttpURLConnection
            try {
                connection.connectTimeout = 10000
                connection.readTimeout = 15000
                connection.setRequestProperty("Accept", "application/json")
                when (connection.responseCode) {
                    200 -> Unit
                    400,
                    404 ->
                        throw CepLookupException(
                            "CEP não encontrado. Confira os números e tente novamente."
                        )
                    else ->
                        throw CepLookupException(
                            "A busca por CEP está indisponível. Tente novamente em instantes."
                        )
                }
                val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                val coordinates = json.optJSONObject("location")?.optJSONObject("coordinates")
                fun text(key: String) = json.optString(key).takeUnless { it == "null" }.orEmpty()
                CepResponse(
                    text("street"),
                    text("neighborhood"),
                    text("city"),
                    text("state"),
                    coordinates?.optString("latitude")?.toDoubleOrNull(),
                    coordinates?.optString("longitude")?.toDoubleOrNull(),
                )
            } finally {
                connection.disconnect()
            }
        }
}
