package com.borarpg.domain.rules.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CepTest {
    @Test
    fun acceptsFormattedAndUnformattedWithLeadingZero() {
        assertEquals("01310100", normalizeCep("01310-100"))
        assertEquals("01310100", normalizeCep(" 01310100 "))
    }

    @Test
    fun rejectsMalformedInput() {
        listOf("", "0131010", "013101000", "abcdefgh", "01310a100", "0131-0100", "01310 100")
            .forEach { assertNull(normalizeCep(it)) }
    }
}
