package com.borarpg.data

import com.borarpg.domain.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NearbyPlacesRepository {
    private val cache = ConcurrentHashMap<String, List<PublicPlace>>()

    suspend fun find(center: CepLocation): List<PublicPlace> =
        withContext(Dispatchers.IO) {
            val key = "${center.latitude},${center.longitude}"
            cache[key]?.let {
                return@withContext it
            }
            val position = String.format(Locale.US, "%f,%f", center.latitude, center.longitude)
            val query =
                """
            [out:json][timeout:20];
            (
              nwr(around:${QuestGenerator.RADIUS_METERS},$position)[leisure~"^(park|recreation_ground|fitness_station)$"][access!~"^(private|no|customers|permit|members)$"];
              nwr(around:${QuestGenerator.RADIUS_METERS},$position)[place=square][access!~"^(private|no|customers|permit|members)$"];
            );
            out center 150;
        """
                    .trimIndent()
            val connection =
                URL("https://overpass-api.de/api/interpreter").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 30000
                connection.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8",
                )
                connection.setRequestProperty(
                    "User-Agent",
                    "BoraRPG/0.1 (https://github.com/andersonms92/BoraRPG)",
                )
                connection.outputStream.use {
                    it.write(
                        ("data=" + URLEncoder.encode(query, "UTF-8")).toByteArray(Charsets.UTF_8)
                    )
                }
                if (connection.responseCode != 200)
                    throw IOException("Consulta de locais indisponível")
                val response =
                    connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                if (response.has("remark")) throw IOException("Consulta de locais incompleta")
                val elements = response.getJSONArray("elements")
                val places =
                    (0 until elements.length()).mapNotNull { i ->
                        val item = elements.getJSONObject(i)
                        val tags = item.optJSONObject("tags") ?: return@mapNotNull null
                        val point = item.optJSONObject("center") ?: item
                        val lat = point.optDouble("lat", Double.NaN)
                        val lon = point.optDouble("lon", Double.NaN)
                        if (!lat.isFinite() || !lon.isFinite()) return@mapNotNull null
                        val label =
                            if (tags.optString("place") == "square") "Praça"
                            else
                                when (tags.optString("leisure")) {
                                    "park" -> "Parque"
                                    "fitness_station" -> "Estação de exercícios"
                                    else -> "Área de lazer"
                                }
                        PublicPlace(
                            "${item.getString("type")}/${item.getLong("id")}",
                            tags.optString("name").ifBlank { label },
                            lat,
                            lon,
                        )
                    }
                cache[key] = places
                places
            } finally {
                connection.disconnect()
            }
        }
}
