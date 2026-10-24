package com.borarpg.domain

data class CepLocation(
    val cep: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

fun normalizeCep(value: String): String? {
    val trimmed = value.trim()
    if (!Regex("[0-9]{5}-?[0-9]{3}").matches(trimmed)) return null
    return trimmed.replace("-", "")
}
