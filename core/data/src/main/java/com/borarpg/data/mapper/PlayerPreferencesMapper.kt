package com.borarpg.data.mapper

import androidx.datastore.preferences.core.Preferences
import com.borarpg.data.local.preferences.PlayerPreferenceKeys
import com.borarpg.domain.model.player.PlayerProfile

internal fun Preferences.toPlayerProfile() =
    PlayerProfile(
        this[PlayerPreferenceKeys.name] ?: "",
        this[PlayerPreferenceKeys.weight]?.replace(',', '.')?.toDoubleOrNull(),
        this[PlayerPreferenceKeys.height]?.replace(',', '.')?.toDoubleOrNull(),
        this[PlayerPreferenceKeys.age]?.toIntOrNull(),
    )
