package com.borarpg.data.mapper

import com.borarpg.domain.error.CepLookupError
import com.borarpg.domain.error.CepLookupException
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import java.io.IOException
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

internal fun Exception.toCepLookupException(): CepLookupException =
    when (this) {
        is CancellationException -> throw this
        is CepLookupException -> this
        is HttpException -> CepLookupException(code().toCepLookupError(), this)
        is JsonDataException,
        is JsonEncodingException -> CepLookupException(CepLookupError.INVALID_RESPONSE, this)
        is IOException -> CepLookupException(CepLookupError.CONNECTION, this)
        else -> throw this
    }

private fun Int.toCepLookupError(): CepLookupError =
    when (this) {
        HTTP_BAD_REQUEST, HTTP_NOT_FOUND -> CepLookupError.NOT_FOUND
        else -> CepLookupError.SERVICE_UNAVAILABLE
    }
