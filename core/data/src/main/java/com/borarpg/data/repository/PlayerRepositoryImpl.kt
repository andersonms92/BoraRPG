package com.borarpg.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.borarpg.data.local.preferences.PlayerPreferenceKeys
import com.borarpg.data.local.preferences.QuestPreferencesCodec
import com.borarpg.data.local.preferences.playerStore
import com.borarpg.data.mapper.toPlayerProfile
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.model.player.PlayerProfile
import com.borarpg.domain.repository.PlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
internal class PlayerRepositoryImpl @Inject constructor(@ApplicationContext context: Context) :
    PlayerRepository {
    private val store = context.applicationContext.playerStore
    private val preferences =
        store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }
    override val profile = preferences.map { it.toPlayerProfile() }
    override val quests = preferences.map {
        QuestPreferencesCodec.decode(it[PlayerPreferenceKeys.quests] ?: "[]")
    }

    override suspend fun save(profile: PlayerProfile) {
        require(profile.complete)
        store.edit { preferences ->
            preferences[PlayerPreferenceKeys.name] = profile.name.trim()
            preferences[PlayerPreferenceKeys.weight] = profile.weightKg.toString()
            preferences[PlayerPreferenceKeys.height] = profile.heightCm.toString()
            preferences[PlayerPreferenceKeys.age] = profile.ageYears.toString()
        }
    }

    override suspend fun addQuest(quest: QuestPoint) {
        store.edit { preferences ->
            preferences[PlayerPreferenceKeys.quests] =
                QuestPreferencesCodec.append(
                    preferences[PlayerPreferenceKeys.quests] ?: "[]",
                    quest,
                )
        }
    }

    override suspend fun deleteQuest(id: String) {
        store.edit { preferences ->
            preferences[PlayerPreferenceKeys.quests] =
                QuestPreferencesCodec.remove(
                    preferences[PlayerPreferenceKeys.quests] ?: "[]",
                    id,
                )
        }
    }
}
