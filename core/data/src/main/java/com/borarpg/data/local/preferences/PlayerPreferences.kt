package com.borarpg.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

internal val Context.playerStore by preferencesDataStore("player")

internal object PlayerPreferenceKeys {
    val name = stringPreferencesKey("name")
    val weight = stringPreferencesKey("weight")
    val height = stringPreferencesKey("height")
    val age = stringPreferencesKey("age")
    val quests = stringPreferencesKey("quests")
}
