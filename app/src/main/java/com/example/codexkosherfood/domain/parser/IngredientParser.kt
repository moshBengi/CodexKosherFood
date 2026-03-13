package com.example.codexkosherfood.domain.parser

import com.example.codexkosherfood.domain.model.ParsedIngredient
import com.example.codexkosherfood.domain.model.ParsedIngredients
import java.util.Locale

class IngredientParser {
    private val headers = listOf(
        "ingredients",
        "ingrÃ©dients",
        "zutaten",
        "ingredientes",
        "ingredienti",
    )

    private val stopMarkers = listOf(
        "nutrition",
        "nutrition facts",
        "nutrition information",
        "allergen",
        "allergens",
        "contains",
        "distributed by",
        "imported by",
        "best before",
        "serving suggestion",
    )

    fun parse(rawText: String): ParsedIngredients {
        val normalizedSource = rawText
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .trim()

        val extractedSection = extractSection(normalizedSource)
        val cleanedSection = extractedSection
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { normalizedSource }

        return ParsedIngredients(
            sourceText = normalizedSource,
            sectionText = cleanedSection,
            items = buildParsedIngredients(cleanedSection),
            header = detectHeader(normalizedSource),
        )
    }

    private fun extractSection(text: String): String {
        val lowerText = text.lowercase(Locale.ROOT)
        val headerMatch = headers
            .mapNotNull { header ->
                val pattern = Regex("""\b${Regex.escape(header)}\b\s*[:\-]?\s*""")
                pattern.find(lowerText)?.let { match -> header to match }
            }
            .minByOrNull { (_, match) -> match.range.first }

        val candidate = if (headerMatch != null) {
            val (_, match) = headerMatch
            text.substring(match.range.last + 1)
        } else {
            text
        }

        val lines = candidate.lines()
        val relevantLines = mutableListOf<String>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) {
                if (relevantLines.isNotEmpty()) {
                    break
                }
                continue
            }
            val lineLower = trimmed.lowercase(Locale.ROOT)
            if (stopMarkers.any { lineLower.startsWith(it) }) {
                break
            }
            relevantLines += trimmed
        }

        return relevantLines.joinToString(separator = " ")
    }

    private fun splitIngredients(section: String): List<String> {
        return section
            .split(Regex("[,;\\u2022]"))
            .map { it.trim().trim('.') }
            .filter { it.isNotBlank() }
    }

    private fun buildParsedIngredients(section: String): List<ParsedIngredient> {
        return splitIngredients(section).map { ingredient ->
            ParsedIngredient(
                originalName = ingredient,
                normalizedName = normalizeIngredientName(ingredient),
            )
        }
    }

    private fun detectHeader(text: String): String? {
        val lowerText = text.lowercase(Locale.ROOT)
        return headers.firstOrNull { lowerText.contains(it) }
    }
}
