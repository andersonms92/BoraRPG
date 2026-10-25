package com.borarpg.data.mapper

import com.borarpg.data.remote.cep.CepResponse
import com.borarpg.domain.error.CepLookupException
import com.borarpg.domain.model.location.CepLocation

internal fun CepResponse.toDomain(cep: String): CepLocation {
    val lat = latitude
    val lon = longitude
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
        listOf(street, neighborhood, city, state).filter { it.isNotBlank() }.joinToString(" · ")
    return CepLocation(
        "${cep.take(5)}-${cep.takeLast(3)}",
        address.ifBlank { "CEP $cep" },
        lat,
        lon,
    )
}
