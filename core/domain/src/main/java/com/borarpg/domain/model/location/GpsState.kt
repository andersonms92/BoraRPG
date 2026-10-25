package com.borarpg.domain.model.location

data class GpsState(
    val location: GeoLocation? = null,
    val message: String = "Buscando sua localização…",
)
