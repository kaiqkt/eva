package dev.kaiqkt.eva.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals

class SlugGeneratorTest {

    @Test
    fun shouldLowercaseAndReplaceSpaces() {
        assertEquals("minha-app", SlugGenerator.fromName("Minha App"))
    }

    @Test
    fun shouldTrimSurroundingWhitespace() {
        assertEquals("minha-app", SlugGenerator.fromName("  Minha App  "))
    }
}
