package com.borarpg.domain.rules.combat

import com.borarpg.domain.model.hero.Hero
import org.junit.Assert.assertEquals
import org.junit.Test

class CombatTest {
    @Test
    fun victoryGrantsRewardOnceAndStartsNextEncounter() {
        val result = Combat.advance(Hero(hp = 250), 250)
        assertEquals(5000, result.hp)
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
        assertEquals(4750, result.hp)
        assertEquals(0, result.gold)
    }

    @Test
    fun formerVictoryThresholdDoesNotDefeatStrongerGuardian() {
        val result = Combat.advance(Hero(), 1996)
        assertEquals(1996, result.steps)
        assertEquals(0, result.victories)
        assertEquals(3004, result.hp)
        assertEquals(199, result.xp)
        assertEquals(0, result.gold)
    }

    @Test
    fun finalFourStepsDefeatSecondGuardianAndStartThird() {
        val first = Combat.advance(Hero(), 9996)
        assertEquals(1, first.victories)
        assertEquals(4, first.hp)
        val result = Combat.advance(first, 4)
        assertEquals(10000, result.steps)
        assertEquals(2, result.victories)
        assertEquals(5000, result.hp)
        assertEquals(1100, result.xp)
        assertEquals(50, result.gold)
    }

    @Test
    fun migratedProgressContinuesWithoutRecalculatingPreviousRewards() {
        val previous = Hero(xp = 699, gold = 158, hp = 4004, victories = 1, steps = 1996)
        val partial = Combat.advance(previous, 4003)
        assertEquals(Hero(xp = 1099, gold = 158, hp = 1, victories = 1, steps = 5999), partial)

        val result = Combat.advance(partial, 1)
        assertEquals(Hero(xp = 1150, gold = 183, hp = 5000, victories = 2, steps = 6000), result)
    }

    @Test
    fun batchCanDefeatSeveralStrongerGuardiansAndCarryRemainingDamage() {
        val result = Combat.advance(Hero(), 12501)
        assertEquals(Hero(xp = 1350, gold = 50, hp = 2499, victories = 2, steps = 12501), result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroSteps() {
        Combat.advance(Hero(), 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeSteps() {
        Combat.advance(Hero(), -1)
    }
}
