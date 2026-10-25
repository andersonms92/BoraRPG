package com.borarpg.domain

data class GeoLocation(val latitude: Double, val longitude: Double, val accuracy: Float = 0f)

data class GpsState(
    val location: GeoLocation? = null,
    val message: String = "Buscando sua localização…",
)
