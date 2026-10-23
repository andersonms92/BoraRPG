package com.borarpg.domain

import org.junit.Assert.*
import org.junit.Test

class ProfileTest {
    @Test fun acceptsCommaDecimals() { assertTrue(PlayerProfile("Ana", "65,5", "170", "30").errors().isEmpty()) }
    @Test fun rejectsMissingOrInvalidMeasurements() {
        val errors = PlayerProfile("", "NaN", "-1", "0").errors()
        assertEquals(setOf("name", "weight", "height", "age"), errors.keys)
    }
    @Test fun acceptsBoundaryValues() { assertTrue(PlayerProfile("Ana", "20", "80", "13").errors().isEmpty()) }
    @Test fun rejectsNonIntegerAge() { assertTrue(PlayerProfile("Ana", "65", "170", "30.5").errors().containsKey("age")) }
}
