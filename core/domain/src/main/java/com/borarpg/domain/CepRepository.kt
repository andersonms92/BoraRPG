package com.borarpg.domain

interface CepRepository {
    suspend fun find(input: String): CepLocation
}
