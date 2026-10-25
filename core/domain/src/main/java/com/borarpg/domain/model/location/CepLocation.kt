package com.borarpg.domain.model.location

data class CepLocation(
    val cep: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)
