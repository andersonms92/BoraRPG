package com.borarpg.data.local.preferences

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.model.mission.QuestRarity
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuestPreferencesCodecTest {
    @Test
    fun readsPortugueseCategoriesSavedByPreviousVersions() {
        val value =
            JSONArray()
                .put(legacyQuest().put("kind", "Combate").put("rarity", "Épico"))
                .toString()
        val quest = QuestPreferencesCodec.decode(value).single()
        assertEquals(QuestKind.COMBAT, quest.kind)
        assertEquals(QuestRarity.EPIC, quest.rarity)
        assertEquals("Relíquia de prata", quest.itemReward)
        assertEquals(200, quest.xpReward)
    }

    @Test
    fun missingAndUnsupportedCategoriesUseExplicitDefaults() {
        val quests =
            listOf(
                legacyQuest(),
                legacyQuest().put("kind", "unknown").put("rarity", "x"),
                legacyQuest().put("kind", JSONObject.NULL).put("rarity", JSONObject.NULL),
            )
        for (quest in quests) {
            val decoded = QuestPreferencesCodec.decode(JSONArray().put(quest).toString()).single()
            assertEquals(QuestKind.EXPLORATION, decoded.kind)
            assertEquals(QuestRarity.COMMON, decoded.rarity)
        }
    }

    @Test
    fun roundTripsAllSupportedCategories() {
        for (kind in QuestKind.entries) {
            for (rarity in QuestRarity.entries) {
                val quest =
                    QuestPoint(
                        id = "new",
                        title = "Praça",
                        latitude = -23.55,
                        longitude = -46.63,
                        kind = kind,
                        rarity = rarity,
                    )
                val encoded = QuestPreferencesCodec.append("[]", quest)
                assertEquals(quest, QuestPreferencesCodec.decode(encoded).single())
            }
        }
    }

    @Test
    fun newPreferencesUseStableUntranslatedCodes() {
        val quest =
            QuestPoint(
                id = "new",
                title = "Praça",
                latitude = -23.55,
                longitude = -46.63,
                kind = QuestKind.COMBAT,
                rarity = QuestRarity.EPIC,
            )
        val json = JSONArray(QuestPreferencesCodec.append("[]", quest)).getJSONObject(0)
        assertEquals("combat", json.getString("kind"))
        assertEquals("epic", json.getString("rarity"))
    }

    private fun legacyQuest() =
        JSONObject()
            .put("id", "legacy")
            .put("title", "Praça")
            .put("lat", -23.55)
            .put("lon", -46.63)
            .put("generated", true)
            .put("goalSteps", 1200)
            .put("xpReward", 200)
            .put("goldReward", 75)
            .put("itemReward", "Relíquia de prata")
}
