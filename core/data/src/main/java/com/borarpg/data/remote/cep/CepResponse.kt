package com.borarpg.data.remote.cep

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class CepResponse(
    val street: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val state: String? = null,
    val location: CepLocationResponse? = null,
)

@JsonClass(generateAdapter = true)
internal data class CepLocationResponse(val coordinates: CepCoordinatesResponse? = null)

@JsonClass(generateAdapter = true)
internal data class CepCoordinatesResponse(
    val latitude: String? = null,
    val longitude: String? = null,
)
