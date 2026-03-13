package com.example.codexkosherfood.domain.rules

import com.example.codexkosherfood.domain.model.IngredientStatus
import com.example.codexkosherfood.domain.model.ParsedIngredient
import com.example.codexkosherfood.domain.model.ParsedIngredients
import com.example.codexkosherfood.domain.model.Verdict
import com.example.codexkosherfood.domain.parser.normalizeIngredientName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KosherRulesEngineTest {
    private val engine = KosherRulesEngine()

    @Test
    fun `returns problem when one ingredient is not kosher`() {
        val parsed = parsedIngredients("Ingredients: water, pork broth, salt")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.PROBLEM, result.verdict)
        assertTrue(result.notKosherItems.any { it.originalName == "pork broth" })
        assertTrue(result.okItems.any { it.originalName == "water" })
    }

    @Test
    fun `returns uncertain when one ingredient needs review`() {
        val parsed = parsedIngredients("Ingredients: sugar, e471")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.UNCERTAIN, result.verdict)
        assertTrue(result.okItems.any { it.originalName == "sugar" })
        assertTrue(result.uncertainItems.any { it.originalName == "e471" })
    }

    @Test
    fun `keeps dairy ingredient uncertain`() {
        val parsed = parsedIngredients("Ingredients: sugar, milk")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.UNCERTAIN, result.verdict)
        assertTrue(result.uncertainItems.any { it.originalName == "milk" })
    }

    @Test
    fun `treats unknown ingredient as uncertain`() {
        val parsed = parsedIngredients("Ingredients: xanthotril, water")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.UNCERTAIN, result.verdict)
        assertTrue(result.uncertainItems.any { it.originalName == "xanthotril" })
        assertTrue(result.uncertainItems.any { it.reason.contains("not recognized", ignoreCase = true) })
    }

    @Test
    fun `keeps vegetable shortening out of uncertain rule matching`() {
        val parsed = parsedIngredients("Ingredients: wheat flour, vegetable shortening, salt")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.UNCERTAIN, result.verdict)
        assertTrue(result.okItems.any { it.originalName == "wheat flour" })
        assertTrue(result.uncertainItems.any { it.originalName == "vegetable shortening" })
        assertTrue(result.uncertainItems.any { it.reason.contains("not recognized", ignoreCase = true) })
    }

    @Test
    fun `returns ok for clean vegetarian list`() {
        val parsed = parsedIngredients("Ingredients: water, sugar, cocoa, sunflower oil")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.OK, result.verdict)
        assertEquals(4, result.okItems.size)
        assertTrue(result.assessments.all { it.status == IngredientStatus.OK })
    }

    @Test
    fun `uses normalized names for ambiguous matching`() {
        val parsed = parsedIngredients("Ingredients: Mono & Diglycerides, Artificial Colour")

        val result = engine.analyze(parsed)

        assertTrue(result.uncertainItems.any { it.normalizedName == "mono and diglycerides" })
        assertTrue(result.uncertainItems.any { it.originalName == "Artificial Colour" })
    }

    private fun parsedIngredients(section: String): ParsedIngredients {
        val cleanSection = section.substringAfter(':').trim()
        return ParsedIngredients(
            sourceText = section,
            sectionText = cleanSection,
            items = cleanSection.split(",").map { item ->
                val original = item.trim()
                ParsedIngredient(
                    originalName = original,
                    normalizedName = normalizeIngredientName(original),
                )
            },
            header = "ingredients",
        )
    }
}
