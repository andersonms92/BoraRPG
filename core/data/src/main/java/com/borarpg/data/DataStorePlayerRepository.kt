package com.borarpg.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.borarpg.domain.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.playerStore by preferencesDataStore("player")

@Singleton
class DataStorePlayerRepository @Inject constructor(@ApplicationContext context: Context) :
    PlayerRepository {
    private val store = context.applicationContext.playerStore
    private val preferences =
        store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }
    override val profile: Flow<PlayerProfile> = preferences.map {
        PlayerProfile(
            it[key("name")] ?: "",
            it[key("weight")]?.replace(',', '.')?.toDoubleOrNull(),
            it[key("height")]?.replace(',', '.')?.toDoubleOrNull(),
            it[key("age")]?.toIntOrNull(),
        )
    }
    override val quests: Flow<List<QuestPoint>> = preferences.map { prefs ->
        val array = JSONArray(prefs[key("quests")] ?: "[]")
        (0 until array.length()).map { index ->
            val q = array.getJSONObject(index)
            QuestPoint(
                q.getString("id"),
                q.getString("title"),
                q.getDouble("lat"),
                q.getDouble("lon"),
                q.optString("kind", "Exploração"),
                q.optBoolean("generated", false),
                q.optString("description", ""),
                q.optInt("goalSteps", 0),
                q.optInt("xpReward", 0),
                q.optInt("goldReward", 0),
                q.optString("itemReward", ""),
                q.optString("rarity", "Comum"),
            )
        }
    }

    override suspend fun save(profile: PlayerProfile) {
        require(profile.complete)
        store.edit { p ->
            p[key("name")] = profile.name.trim()
            p[key("weight")] = profile.weightKg.toString()
            p[key("height")] = profile.heightCm.toString()
            p[key("age")] = profile.ageYears.toString()
        }
    }

    override suspend fun addQuest(quest: QuestPoint) {
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
                    .put("kind", quest.kind)
                    .put("generated", quest.generated)
                    .put("description", quest.description)
                    .put("goalSteps", quest.goalSteps)
                    .put("xpReward", quest.xpReward)
                    .put("goldReward", quest.goldReward)
                    .put("itemReward", quest.itemReward)
                    .put("rarity", quest.rarity)
            )
            p[key("quests")] = array.toString()
        }
    }

    override suspend fun deleteQuest(id: String) {
        store.edit { p ->
            val old = JSONArray(p[key("quests")] ?: "[]")
            val next = JSONArray()
            for (i in 0 until old.length()) if (old.getJSONObject(i).getString("id") != id)
                next.put(old.getJSONObject(i))
            p[key("quests")] = next.toString()
        }
    }

    private fun key(name: String) = stringPreferencesKey(name)
}
