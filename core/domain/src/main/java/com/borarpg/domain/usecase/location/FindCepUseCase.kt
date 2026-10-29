package com.borarpg.domain.usecase.location

import com.borarpg.domain.error.CepLookupError
import com.borarpg.domain.error.CepLookupException
import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.repository.CepRepository
import com.borarpg.domain.rules.location.normalizeCep

class FindCepUseCase(private val repository: CepRepository) {
    suspend operator fun invoke(input: String): CepLocation {
        val cep = normalizeCep(input)
            ?: throw CepLookupException(CepLookupError.INVALID_CEP)
        return repository.find(cep)
    }
}
