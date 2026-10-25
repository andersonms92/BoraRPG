package com.borarpg.data.local.preferences

import com.borarpg.domain.model.mission.QuestPoint
import org.json.JSONArray
import org.json.JSONObject

internal object QuestPreferencesCodec {
    fun decode(value: String): List<QuestPoint> {
        val array = JSONArray(value)
        return (0 until array.length()).map { index ->
            val quest = array.getJSONObject(index)
            QuestPoint(
                quest.getString("id"),
                quest.getString("title"),
                quest.getDouble("lat"),
                quest.getDouble("lon"),
                quest.optString("kind", "Exploração"),
                quest.optBoolean("generated", false),
                quest.optString("description", ""),
                quest.optInt("goalSteps", 0),
                quest.optInt("xpReward", 0),
                quest.optInt("goldReward", 0),
                quest.optString("itemReward", ""),
                quest.optString("rarity", "Comum"),
            )
        }
    }

    fun append(value: String, quest: QuestPoint): String =
        JSONArray(value).put(quest.toJson()).toString()

    fun remove(value: String, id: String): String {
        val previous = JSONArray(value)
        val next = JSONArray()
        for (index in 0 until previous.length()) {
            val quest = previous.getJSONObject(index)
            if (quest.getString("id") != id) next.put(quest)
        }
        return next.toString()
    }

    private fun QuestPoint.toJson() =
        JSONObject()
            .put("id", id)
            .put("title", title.trim())
            .put("lat", latitude)
            .put("lon", longitude)
            .put("kind", kind)
            .put("generated", generated)
            .put("description", description)
            .put("goalSteps", goalSteps)
            .put("xpReward", xpReward)
            .put("goldReward", goldReward)
            .put("itemReward", itemReward)
            .put("rarity", rarity)
}
