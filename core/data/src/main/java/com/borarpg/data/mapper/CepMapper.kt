package com.borarpg.data.mapper

import com.borarpg.data.remote.cep.CepResponse
import com.borarpg.domain.error.CepLookupError
import com.borarpg.domain.error.CepLookupException
import com.borarpg.domain.model.location.CepLocation

internal fun CepResponse.toDomain(cep: String): CepLocation {
    val lat = location?.coordinates?.latitude?.toDoubleOrNull()
    val lon = location?.coordinates?.longitude?.toDoubleOrNull()
    if (
        lat == null ||
            lon == null ||
            !lat.isFinite() ||
            !lon.isFinite() ||
            lat !in -90.0..90.0 ||
            lon !in -180.0..180.0 ||
            (lat == 0.0 && lon == 0.0)
    ) {
        throw CepLookupException(CepLookupError.LOCATION_UNAVAILABLE)
    }
    val address =
        listOfNotNull(street, neighborhood, city, state)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
    return CepLocation(
        "${cep.take(5)}-${cep.takeLast(3)}",
        address.ifBlank { "CEP $cep" },
        lat,
        lon,
    )
}
