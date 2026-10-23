package com.borarpg.domain

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class QuestGeneratorTest {
    private val center = CepLocation("01310-100", "São Paulo", -23.56, -46.65)

    @Test
    fun onlyUsesRealCandidatesWithinRadius() {
        val near = PublicPlace("node/1", "Praça", -23.561, -46.65)
        val far = PublicPlace("node/2", "Distante", -24.0, -46.65)
        val result = QuestGenerator.generate(center, listOf(near, far), Random(1))
        assertEquals(1, result.size)
        assertEquals(near.latitude, result.first().latitude, 0.0)
        assertTrue(result.first().generated)
    }

    @Test
    fun limitsCountAndRemovesOverlappingPoints() {
        val places =
            (1..20).map { PublicPlace("node/$it", "Praça $it", -23.56 + it * .0008, -46.65) }
        val result = QuestGenerator.generate(center, places + places.first(), Random(2))
        assertEquals(6, result.size)
        assertEquals(result.size, result.map { it.id }.distinct().size)
    }

    @Test
    fun emptyCoverageDoesNotInventCoordinates() {
        assertTrue(QuestGenerator.generate(center, emptyList(), Random(1)).isEmpty())
    }

    @Test
    fun supportsDifferentRandomSelections() {
        val places =
            (1..18).map { PublicPlace("node/$it", "Praça $it", -23.56 + it * .0008, -46.65) }
        assertNotEquals(
            QuestGenerator.generate(center, places, Random(1)),
            QuestGenerator.generate(center, places, Random(9)),
        )
    }
}
