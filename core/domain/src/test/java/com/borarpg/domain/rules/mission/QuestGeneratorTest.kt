package com.borarpg.domain.rules.mission

import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.model.mission.MissionProgress
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestGeneratorTest {
    private val center = CepLocation("01310-100", "São Paulo", -23.56, -46.65)

    @Test
    fun generatesSixUniqueRewardingEncounters() {
        val quests = QuestGenerator.generate(center, Random(1))
        assertEquals(6, quests.size)
        assertEquals(6, quests.map { it.id }.distinct().size)
        assertTrue(
            quests.all {
                it.goalSteps > 0 &&
                    it.xpReward > 0 &&
                    it.goldReward > 0 &&
                    it.itemReward.isNotBlank()
            }
        )
        assertEquals(setOf("Combate", "Exploração"), quests.map { it.kind }.toSet())
    }

    @Test
    fun placesStayNearThePostalCode() {
        QuestGenerator.generate(center, Random(2)).forEach {
            assertTrue(kotlin.math.abs(it.latitude - center.latitude) < .01)
            assertTrue(kotlin.math.abs(it.longitude - center.longitude) < .011)
        }
    }

    @Test
    fun rarerEncountersHaveLargerGoalsAndRewards() {
        val quests = QuestGenerator.generate(center, Random(3))
        val common = quests.first { it.rarity == "Comum" }
        val epic = quests.first { it.rarity == "Épico" }
        assertTrue(epic.goalSteps > common.goalSteps)
        assertTrue(epic.xpReward > common.xpReward)
        assertTrue(epic.goldReward > common.goldReward)
    }

    @Test
    fun anotherRollChangesThePositions() {
        assertNotEquals(
            QuestGenerator.generate(center, Random(1)).map { it.latitude },
            QuestGenerator.generate(center, Random(2)).map { it.latitude },
        )
    }

    @Test
    fun progressOnlyUsesStepsAfterAcceptance() {
        val quest = QuestGenerator.generate(center, Random(1)).first()
        assertEquals(0, MissionProgress(quest, 10000, 10000).steps)
        assertFalse(MissionProgress(quest, 10000, 10000).ready)
        assertTrue(MissionProgress(quest, 10000, 10000 + quest.goalSteps).ready)
        assertEquals(quest.goalSteps, MissionProgress(quest, 10000, 50000).steps)
    }
}
