package com.borarpg.domain.error

enum class CepLookupError {
    INVALID_CEP,
    NOT_FOUND,
    LOCATION_UNAVAILABLE,
    SERVICE_UNAVAILABLE,
    CONNECTION,
    INVALID_RESPONSE,
}
