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
        assertEquals(listOf("Sugar", "Water", "Natural Flavors", "Citric Acid"), parsed.items)
    }

    @Test
    fun `extracts french header and stops before allergens`() {
        val rawText = """
            Ingrédients : eau, sucre, gélatine, arômes naturels
            Allergens: milk
        """.trimIndent()

        val parsed = parser.parse(rawText)

        assertEquals("ingrédients", parsed.header)
        assertEquals("eau, sucre, gélatine, arômes naturels", parsed.sectionText)
        assertTrue(parsed.items.contains("gélatine"))
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
