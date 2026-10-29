package com.borarpg.domain.rules.workout

import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.rules.combat.Combat
import org.junit.Assert.assertEquals
import org.junit.Test

class StepCounterTest {
    @Test
    fun ignoresStepsBeforeActivityAndDuplicateReadings() {
        val counter = StepCounter()
        assertEquals(0, counter.record(12000))
        assertEquals(0, counter.record(12000))
        assertEquals(7, counter.record(12007))
        assertEquals(2, counter.record(12009))
    }

    @Test
    fun resetsBaselineWhenDeviceCounterRestarts() {
        val counter = StepCounter()
        counter.record(12000)
        assertEquals(0, counter.record(2))
        assertEquals(3, counter.record(5))
    }

    @Test
    fun newActivityDoesNotCountStepsBetweenSessions() {
        val first = StepCounter()
        first.record(100)
        assertEquals(10, first.record(110))
        val second = StepCounter()
        assertEquals(0, second.record(500))
        assertEquals(1, second.record(501))
    }

    @Test
    fun rewardsDoNotDependOnSensorBatchSize() {
        val batched = Combat.advance(Hero(), 12501)
        val individual = (1..12501).fold(Hero()) { hero, _ -> Combat.advance(hero, 1) }
        assertEquals(batched, individual)
        assertEquals(2, batched.victories)
        assertEquals(2499, batched.hp)
    }
}
