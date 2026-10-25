package com.borarpg.feature.profile

import org.junit.Assert.*
import org.junit.Test

class ProfileFormTest {
    @Test
    fun convertsCommaDecimalsToTypedMeasurements() {
        val profile = ProfileForm("Ana", "65,5", "170,5", "30").toDomain()
        assertEquals(65.5, profile.weightKg!!, 0.0)
        assertEquals(170.5, profile.heightCm!!, 0.0)
        assertEquals(30, profile.ageYears)
    }

    @Test
    fun acceptsCommaDecimals() {
        assertTrue(ProfileForm("Ana", "65,5", "170", "30").errors().isEmpty())
    }

    @Test
    fun rejectsMissingOrInvalidMeasurements() {
        val errors = ProfileForm("", "NaN", "-1", "0").errors()
        assertEquals(setOf("name", "weight", "height", "age"), errors.keys)
    }

    @Test
    fun acceptsBoundaryValues() {
        assertTrue(ProfileForm("Ana", "20", "80", "13").errors().isEmpty())
    }

    @Test
    fun rejectsNonIntegerAge() {
        assertTrue(ProfileForm("Ana", "65", "170", "30.5").errors().containsKey("age"))
    }
}
