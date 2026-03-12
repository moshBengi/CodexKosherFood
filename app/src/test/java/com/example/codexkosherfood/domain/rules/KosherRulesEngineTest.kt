package com.example.codexkosherfood.domain.rules

import com.example.codexkosherfood.domain.model.IngredientStatus
import com.example.codexkosherfood.domain.model.ParsedIngredients
import com.example.codexkosherfood.domain.model.Verdict
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
        assertTrue(result.notKosherItems.any { it.ingredient == "pork broth" })
        assertTrue(result.okItems.any { it.ingredient == "water" })
    }

    @Test
    fun `returns uncertain when one ingredient needs review`() {
        val parsed = parsedIngredients("Ingredients: sugar, e471")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.UNCERTAIN, result.verdict)
        assertTrue(result.okItems.any { it.ingredient == "sugar" })
        assertTrue(result.uncertainItems.any { it.ingredient == "e471" })
    }

    @Test
    fun `keeps dairy ingredient uncertain`() {
        val parsed = parsedIngredients("Ingredients: sugar, milk")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.UNCERTAIN, result.verdict)
        assertTrue(result.uncertainItems.any { it.ingredient == "milk" })
    }

    @Test
    fun `treats unknown ingredient as uncertain`() {
        val parsed = parsedIngredients("Ingredients: xanthotril, water")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.UNCERTAIN, result.verdict)
        assertTrue(result.uncertainItems.any { it.ingredient == "xanthotril" })
        assertTrue(result.uncertainItems.any { it.reason.contains("not recognized", ignoreCase = true) })
    }

    @Test
    fun `keeps vegetable shortening out of uncertain rule matching`() {
        val parsed = parsedIngredients("Ingredients: wheat flour, vegetable shortening, salt")

        val result = engine.analyze(parsed)

        assertEquals(Verdict.UNCERTAIN, result.verdict)
        assertTrue(result.okItems.any { it.ingredient == "wheat flour" })
        assertTrue(result.uncertainItems.any { it.ingredient == "vegetable shortening" })
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

    private fun parsedIngredients(section: String): ParsedIngredients {
        val cleanSection = section.substringAfter(':').trim()
        return ParsedIngredients(
            sourceText = section,
            sectionText = cleanSection,
            items = cleanSection.split(",").map { it.trim() },
            header = "ingredients",
        )
    }
}
