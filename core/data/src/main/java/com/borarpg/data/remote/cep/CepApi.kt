package com.borarpg.data.remote.cep

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

internal interface CepApi {
    @GET("api/cep/v2/{cep}")
    @Headers("Accept: application/json")
    suspend fun find(@Path("cep") cep: String): Response<CepResponse>
}
