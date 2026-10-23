package com.borarpg.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.borarpg.domain.*
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.playerStore by preferencesDataStore("player")

class PlayerRepository(context: Context) {
    private val store = context.applicationContext.playerStore
    private val preferences =
        store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }
    val profile: Flow<PlayerProfile> = preferences.map {
        PlayerProfile(
            it[key("name")] ?: "",
            it[key("weight")] ?: "",
            it[key("height")] ?: "",
            it[key("age")] ?: "",
        )
    }
    val quests: Flow<List<QuestPoint>> = preferences.map { prefs ->
        val array = JSONArray(prefs[key("quests")] ?: "[]")
        (0 until array.length()).map { index ->
            val q = array.getJSONObject(index)
            QuestPoint(
                q.getString("id"),
                q.getString("title"),
                q.getDouble("lat"),
                q.getDouble("lon"),
                "Exploração",
            )
        }
    }

    suspend fun save(profile: PlayerProfile) {
        require(profile.errors().isEmpty())
        store.edit { p ->
            p[key("name")] = profile.name.trim()
            p[key("weight")] = profile.weight
            p[key("height")] = profile.height
            p[key("age")] = profile.age
        }
    }

    suspend fun addQuest(quest: QuestPoint) {
        require(quest.title.isNotBlank() && quest.title.length <= 60)
        require(quest.latitude in -90.0..90.0 && quest.longitude in -180.0..180.0)
        store.edit { p ->
            val array = JSONArray(p[key("quests")] ?: "[]")
            array.put(
                JSONObject()
                    .put("id", quest.id)
                    .put("title", quest.title.trim())
                    .put("lat", quest.latitude)
                    .put("lon", quest.longitude)
            )
            p[key("quests")] = array.toString()
        }
    }

    suspend fun deleteQuest(id: String) = store.edit { p ->
        val old = JSONArray(p[key("quests")] ?: "[]")
        val next = JSONArray()
        for (i in 0 until old.length()) if (old.getJSONObject(i).getString("id") != id)
            next.put(old.getJSONObject(i))
        p[key("quests")] = next.toString()
    }

    private fun key(name: String) = stringPreferencesKey(name)
}
