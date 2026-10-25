package com.borarpg.domain.rules.progression

import com.borarpg.domain.model.hero.Hero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelProgressionTest {
    @Test
    fun firstLevelRequiresOneThousandXp() {
        val hero = Hero()
        assertEquals(1, hero.level)
        assertEquals(0L, hero.xpInLevel)
        assertEquals(1000L, hero.xpForNextLevel)
        assertEquals(1000L, hero.xpToNextLevel)
        assertEquals(0f, hero.levelProgress, 0f)
    }

    @Test
    fun levelChangesOnlyAtProgressiveThresholds() {
        val thresholds = listOf(0, 1000, 2250, 3750, 5500, 7500)
        thresholds.forEachIndexed { index, xp ->
            val hero = Hero(xp = xp)
            assertEquals(index + 1, hero.level)
            assertEquals(0L, hero.xpInLevel)
            assertEquals(1000L + 250L * index, hero.xpForNextLevel)
            if (xp > 0) {
                val previous = Hero(xp = xp - 1)
                assertEquals(index, previous.level)
                assertEquals(1L, previous.xpToNextLevel)
            }
        }
    }

    @Test
    fun existingXpIsPreservedWithSlowerProgression() {
        val hero = Hero(xp = 699, gold = 158, victories = 1, steps = 1996, hp = 4)
        assertEquals(699, hero.xp)
        assertEquals(1, hero.level)
        assertEquals(699L, hero.xpInLevel)
        assertEquals(301L, hero.xpToNextLevel)
        assertEquals(.699f, hero.levelProgress, .00001f)
    }

    @Test
    fun progressUsesXpEarnedWithinCurrentLevel() {
        val hero = Hero(xp = 1700)
        assertEquals(2, hero.level)
        assertEquals(700L, hero.xpInLevel)
        assertEquals(1250L, hero.xpForNextLevel)
        assertEquals(550L, hero.xpToNextLevel)
        assertEquals(.56f, hero.levelProgress, .00001f)
    }

    @Test
    fun maximumStoredXpDoesNotOverflowThresholdCalculations() {
        val hero = Hero(xp = Int.MAX_VALUE)
        assertEquals(4142, hero.level)
        assertEquals(375147L, hero.xpInLevel)
        assertEquals(1036250L, hero.xpForNextLevel)
        assertEquals(661103L, hero.xpToNextLevel)
        assertTrue(hero.levelProgress > 0f && hero.levelProgress < 1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeExperience() {
        LevelProgression.fromTotalXp(-1)
    }
}
