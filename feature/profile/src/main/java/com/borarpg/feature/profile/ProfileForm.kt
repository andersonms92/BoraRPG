package com.borarpg.feature.profile

import com.borarpg.domain.PlayerProfile

data class ProfileForm(
    val name: String = "",
    val weight: String = "",
    val height: String = "",
    val age: String = "",
) {
    fun toDomain(): PlayerProfile {
        require(errors().isEmpty())
        return PlayerProfile(
            name.trim(),
            weight.replace(',', '.').toDouble(),
            height.replace(',', '.').toDouble(),
            age.toInt(),
        )
    }

    companion object {
        fun from(profile: PlayerProfile) =
            ProfileForm(
                profile.name,
                profile.weightKg?.toString().orEmpty(),
                profile.heightCm?.toString().orEmpty(),
                profile.ageYears?.toString().orEmpty(),
            )
    }

    fun errors(): Map<String, String> = buildMap {
        if (name.isBlank() || name.trim().length > 40)
            put("name", "Informe um nome de até 40 caracteres.")
        val kg = weight.replace(',', '.').toDoubleOrNull()
        if (kg == null || !kg.isFinite() || kg !in 20.0..400.0)
            put("weight", "Informe um peso entre 20 e 400 kg.")
        val cm = height.replace(',', '.').toDoubleOrNull()
        if (cm == null || !cm.isFinite() || cm !in 80.0..250.0)
            put("height", "Informe uma altura entre 80 e 250 cm.")
        if (age.toIntOrNull() !in 13..120) put("age", "Informe uma idade entre 13 e 120 anos.")
    }
}
