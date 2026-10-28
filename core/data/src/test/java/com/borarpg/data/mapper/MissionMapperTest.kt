package com.borarpg.data.mapper

import com.borarpg.data.local.entity.MissionEntity
import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestRarity
import org.junit.Assert.assertEquals
import org.junit.Test

class MissionMapperTest {
    private val legacy =
        MissionEntity(
            id = "legacy",
            title = "Guardião antigo",
            latitude = -23.55,
            longitude = -46.63,
            kind = "Combate",
            description = "Objetivo salvo em versão anterior",
            goalSteps = 1200,
            xpReward = 200,
            goldReward = 75,
            itemReward = "Relíquia de prata",
            rarity = "Épico",
            baselineSteps = 5678,
        )

    @Test
    fun loadsLegacyRoomCategoriesWithoutChangingRewards() {
        val quest = legacy.toDomain()
        assertEquals(QuestKind.COMBAT, quest.kind)
        assertEquals(QuestRarity.EPIC, quest.rarity)
        assertEquals(legacy.goalSteps, quest.goalSteps)
        assertEquals(legacy.xpReward, quest.xpReward)
        assertEquals(legacy.goldReward, quest.goldReward)
        assertEquals(legacy.itemReward, quest.itemReward)
    }

    @Test
    fun storesStableCodesAndPreservesMissionData() {
        assertEquals(
            legacy.copy(kind = "combat", rarity = "epic"),
            legacy.toDomain().toEntity(legacy.baselineSteps),
        )
    }

    @Test
    fun loadsUnknownRoomCategoriesWithoutCrashing() {
        val quest = legacy.copy(kind = "unsupported", rarity = "legendary").toDomain()
        assertEquals(QuestKind.EXPLORATION, quest.kind)
        assertEquals(QuestRarity.COMMON, quest.rarity)
        assertEquals(legacy.id, quest.id)
        assertEquals(legacy.xpReward, quest.xpReward)
    }
}
