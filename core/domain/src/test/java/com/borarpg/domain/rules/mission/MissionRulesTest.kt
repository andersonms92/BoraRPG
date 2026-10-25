package com.borarpg.domain.rules.mission

import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.model.mission.MissionProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionRulesTest {
    private val quest =
        QuestGenerator.generate(CepLocation("01310100", "Rua", -23.55, -46.63)).first()

    @Test
    fun acceptsGeneratedMissionWithinLimits() {
        MissionRules.validate(quest)
    }

    @Test
    fun rejectsInvalidGoalsOrRewards() {
        val invalid =
            listOf(
                quest.copy(goalSteps = 199),
                quest.copy(goalSteps = 5001),
                quest.copy(xpReward = 0),
                quest.copy(xpReward = 1001),
                quest.copy(goldReward = -1),
                quest.copy(goldReward = 1001),
                quest.copy(itemReward = ""),
                quest.copy(generated = false),
            )
        invalid.forEach { assertTrue(runCatching { MissionRules.validate(it) }.isFailure) }
    }

    @Test
    fun cannotRewardAnIncompleteMission() {
        val hero = Hero(steps = quest.goalSteps - 1)
        val progress = MissionProgress(quest, 0, hero.steps)
        assertTrue(runCatching { MissionRules.reward(hero, progress) }.isFailure)
    }

    @Test
    fun rewardsPreserveCombatAndStepProgress() {
        val hero = Hero(xp = 25, gold = 10, hp = 450, victories = 3, steps = quest.goalSteps + 100)
        val result = MissionRules.reward(hero, MissionProgress(quest, 100, hero.steps))
        assertEquals(hero.copy(xp = 25 + quest.xpReward, gold = 10 + quest.goldReward), result)
    }
}
