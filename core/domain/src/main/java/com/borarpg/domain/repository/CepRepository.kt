package com.borarpg.domain.repository

import com.borarpg.domain.model.location.CepLocation

interface CepRepository {
    suspend fun find(input: String): CepLocation
}
