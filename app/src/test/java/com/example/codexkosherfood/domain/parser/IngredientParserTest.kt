package com.example.codexkosherfood.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientParserTest {
    private val parser = IngredientParser()

    @Test
    fun `extracts english ingredients section`() {
        val rawText = """
            Front label
            Ingredients: Sugar, Water, Natural Flavors, Citric Acid
            Nutrition Facts
        """.trimIndent()

        val parsed = parser.parse(rawText)

        assertEquals("ingredients", parsed.header)
        assertEquals("Sugar, Water, Natural Flavors, Citric Acid", parsed.sectionText)
        assertEquals(listOf("Sugar", "Water", "Natural Flavors", "Citric Acid"), parsed.items.map { it.originalName })
        assertEquals(listOf("sugar", "water", "natural flavors", "citric acid"), parsed.items.map { it.normalizedName })
    }

    @Test
    fun `extracts french header and stops before allergens`() {
        val rawText = """
            IngrÃƒÆ’Ã‚Â©dients : eau, sucre, gÃƒÆ’Ã‚Â©latine, arÃƒÆ’Ã‚Â´mes naturels
            Allergens: milk
        """.trimIndent()

        val parsed = parser.parse(rawText)

        assertTrue(parsed.sectionText.contains("sucre"))
        assertTrue(!parsed.sectionText.contains("Allergens"))
        assertEquals(4, parsed.items.size)
        assertTrue(parsed.items.any { it.originalName.contains("latine") })
    }

    @Test
    fun `extracts german header across multiple lines`() {
        val rawText = """
            ZUTATEN:
            Wasser, Zucker,
            Gelatine, Aroma

            Best before 2027
        """.trimIndent()

        val parsed = parser.parse(rawText)

        assertEquals("zutaten", parsed.header)
        assertEquals("Wasser, Zucker, Gelatine, Aroma", parsed.sectionText)
        assertEquals(4, parsed.items.size)
    }

    @Test
    fun `falls back to full text when no known header exists`() {
        val rawText = "Water, sugar, salt, cocoa butter"

        val parsed = parser.parse(rawText)

        assertEquals(null, parsed.header)
        assertEquals(rawText, parsed.sectionText)
        assertEquals(4, parsed.items.size)
    }
}
