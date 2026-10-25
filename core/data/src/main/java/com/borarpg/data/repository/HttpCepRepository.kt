package com.borarpg.data.repository

import com.borarpg.data.mapper.toDomain
import com.borarpg.data.remote.cep.CepRemoteDataSource
import com.borarpg.domain.error.CepLookupException
import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.repository.CepRepository
import com.borarpg.domain.rules.location.normalizeCep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpCepRepository @Inject constructor(private val source: CepRemoteDataSource) :
    CepRepository {
    override suspend fun find(input: String): CepLocation {
        val cep =
            normalizeCep(input) ?: throw CepLookupException("Digite um CEP válido com 8 números.")
        return source.find(cep).toDomain(cep)
    }
}
