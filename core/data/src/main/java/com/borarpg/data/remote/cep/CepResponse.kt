package com.borarpg.data.remote.cep

data class CepResponse(
    val street: String,
    val neighborhood: String,
    val city: String,
    val state: String,
    val latitude: Double?,
    val longitude: Double?,
)
