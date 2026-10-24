package com.borarpg.data

import com.borarpg.domain.CepLocation
import com.borarpg.domain.normalizeCep
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CepLookupException(message: String) : IOException(message)

@Singleton
class CepRepository @Inject constructor() {
    suspend fun find(input: String): CepLocation =
        withContext(Dispatchers.IO) {
            val cep =
                normalizeCep(input)
                    ?: throw CepLookupException("Digite um CEP válido com 8 números.")
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
                val lat = coordinates?.optString("latitude")?.toDoubleOrNull()
                val lon = coordinates?.optString("longitude")?.toDoubleOrNull()
                if (
                    lat == null ||
                        lon == null ||
                        !lat.isFinite() ||
                        !lon.isFinite() ||
                        lat !in -90.0..90.0 ||
                        lon !in -180.0..180.0 ||
                        (lat == 0.0 && lon == 0.0)
                ) {
                    throw CepLookupException(
                        "O CEP existe, mas não possui posição no mapa. Tente um CEP próximo ou mova o mapa manualmente."
                    )
                }
                val address =
                    listOf("street", "neighborhood", "city", "state")
                        .map {
                            json.optString(it).takeUnless { value -> value == "null" }.orEmpty()
                        }
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                CepLocation(
                    "${cep.take(5)}-${cep.takeLast(3)}",
                    address.ifBlank { "CEP $cep" },
                    lat,
                    lon,
                )
            } finally {
                connection.disconnect()
            }
        }
}
