package com.borarpg.data.mapper

import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestRarity
import org.junit.Assert.assertEquals
import org.junit.Test

class QuestCategoryMapperTest {
    @Test
    fun readsLegacyKindsAndStableCodes() {
        for (value in listOf("Combate", "combate", "COMBAT", " combat ")) {
            assertEquals(QuestKind.COMBAT, value.toQuestKind())
        }
        for (value in listOf("Exploração", "exploracao", "EXPLORATION")) {
            assertEquals(QuestKind.EXPLORATION, value.toQuestKind())
        }
    }

    @Test
    fun readsLegacyRaritiesAndStableCodes() {
        for (value in listOf("Comum", "comum", "COMMON")) {
            assertEquals(QuestRarity.COMMON, value.toQuestRarity())
        }
        for (value in listOf("Raro", "raro", "RARE")) {
            assertEquals(QuestRarity.RARE, value.toQuestRarity())
        }
        for (value in listOf("Épico", "epico", "EPIC", " épico ")) {
            assertEquals(QuestRarity.EPIC, value.toQuestRarity())
        }
    }

    @Test
    fun unsupportedOrMissingCategoriesHaveExplicitDefaults() {
        for (value in listOf("", "   ", "unknown", "legendary")) {
            assertEquals(QuestKind.EXPLORATION, value.toQuestKind())
            assertEquals(QuestRarity.COMMON, value.toQuestRarity())
        }
    }

    @Test
    fun roundTripsEveryCategoryWithStableUntranslatedCodes() {
        assertEquals(listOf("combat", "exploration"), QuestKind.entries.map { it.toStorageValue() })
        assertEquals(
            listOf("common", "rare", "epic"),
            QuestRarity.entries.map { it.toStorageValue() },
        )
        for (kind in QuestKind.entries) {
            assertEquals(kind, kind.toStorageValue().toQuestKind())
        }
        for (rarity in QuestRarity.entries) {
            assertEquals(rarity, rarity.toStorageValue().toQuestRarity())
        }
    }
}
