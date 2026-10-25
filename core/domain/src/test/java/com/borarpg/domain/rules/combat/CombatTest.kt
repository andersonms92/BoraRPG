package com.borarpg.domain.rules.combat

import com.borarpg.domain.model.hero.Hero
import org.junit.Assert.assertEquals
import org.junit.Test

class CombatTest {
    @Test
    fun victoryGrantsRewardOnceAndStartsNextEncounter() {
        val result = Combat.advance(Hero(hp = 250), 250)
        assertEquals(1000, result.hp)
        assertEquals(75, result.xp)
        assertEquals(25, result.gold)
        assertEquals(1, result.victories)
        val next = Combat.advance(result, 250)
        assertEquals(25, next.gold)
        assertEquals(1, next.victories)
    }

    @Test
    fun partialProgressPersistsWithoutReward() {
        val result = Combat.advance(Hero(), 250)
        assertEquals(750, result.hp)
        assertEquals(0, result.gold)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeSteps() {
        Combat.advance(Hero(), -1)
    }
}
