package com.borarpg.domain

import kotlin.math.*
import kotlin.random.Random

data class PublicPlace(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

object QuestGenerator {
    const val RADIUS_METERS = 2000
    const val MAX_OBJECTIVES = 6

    fun generate(
        center: CepLocation,
        places: List<PublicPlace>,
        random: Random = Random.Default,
    ): List<QuestPoint> {
        val selected = mutableListOf<PublicPlace>()
        places
            .filter {
                it.latitude.isFinite() &&
                    it.longitude.isFinite() &&
                    it.latitude in -90.0..90.0 &&
                    it.longitude in -180.0..180.0
            }
            .distinctBy { it.id }
            .shuffled(random)
            .forEach { place ->
                if (
                    selected.size < MAX_OBJECTIVES &&
                        distance(
                            center.latitude,
                            center.longitude,
                            place.latitude,
                            place.longitude,
                        ) <= RADIUS_METERS &&
                        selected.none {
                            distance(it.latitude, it.longitude, place.latitude, place.longitude) <
                                80
                        }
                )
                    selected += place
            }
        return selected.map { place ->
            QuestPoint(
                id = "osm:${place.id}",
                title = place.name.take(60),
                latitude = place.latitude,
                longitude = place.longitude,
                kind = "Exploração",
                generated = true,
                description =
                    listOf("Descubra", "Explore", "Caminhe até").random(random) + " ${place.name}.",
            )
        }
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat = Math.toRadians(lat2 - lat1)
        val lon = Math.toRadians(lon2 - lon1)
        val a =
            sin(lat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(lon / 2).pow(2)
        return 6371000 * 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}
