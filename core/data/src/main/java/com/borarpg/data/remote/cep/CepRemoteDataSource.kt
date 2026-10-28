package com.borarpg.data.remote.cep

import javax.inject.Inject
import retrofit2.HttpException

internal class CepRemoteDataSource @Inject constructor(private val api: CepApi) {
    suspend fun find(cep: String): CepResponse? {
        val response = api.find(cep)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()
    }
}
