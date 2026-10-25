package com.borarpg.domain.model.player

data class PlayerProfile(
    val name: String = "",
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val ageYears: Int? = null,
) {
    init {
        require(name.length <= 40)
        require(weightKg == null || weightKg.isFinite() && weightKg in 20.0..400.0)
        require(heightCm == null || heightCm.isFinite() && heightCm in 80.0..250.0)
        require(ageYears == null || ageYears in 13..120)
    }

    val complete: Boolean
        get() = name.isNotBlank() && weightKg != null && heightCm != null && ageYears != null
}
