package com.example.codexkosherfood.domain.rules

import com.example.codexkosherfood.domain.model.IngredientAssessment
import com.example.codexkosherfood.domain.model.IngredientStatus
import com.example.codexkosherfood.domain.model.KosherResult
import com.example.codexkosherfood.domain.model.ParsedIngredients
import com.example.codexkosherfood.domain.model.Verdict
import java.util.Locale

class KosherRulesEngine {
    private data class Rule(
        val keywords: List<String>,
        val status: IngredientStatus,
        val reason: String,
        val skipWhen: ((String) -> Boolean)? = null,
    )

    private val okRules = listOf(
        rule(listOf("water", "מים"), IngredientStatus.OK, "Basic neutral ingredient"),
        rule(listOf("salt", "sea salt", "מלח"), IngredientStatus.OK, "Basic neutral ingredient"),
        rule(listOf("sugar", "cane sugar", "sucrose", "סוכר"), IngredientStatus.OK, "Basic neutral ingredient"),
        rule(listOf("citric acid", "חומצת לימון"), IngredientStatus.OK, "Basic neutral ingredient"),
        rule(listOf("baking soda", "sodium bicarbonate", "סודה לשתייה"), IngredientStatus.OK, "Basic neutral ingredient"),
        rule(
            listOf(
                "vegetables", "fruits", "legumes", "grains", "nuts",
                "olive oil", "sunflower oil", "canola oil", "soybean oil", "coconut oil", "vegetable oil",
                "spice", "spices", "carrot", "onion", "garlic", "tomato", "potato", "apple", "banana", "orange",
                "rice", "corn", "wheat", "flour", "bean", "beans", "lentil", "chickpea", "oat", "oats",
                "almond", "walnut", "cashew", "hazelnut", "pistachio", "cocoa", "cocoa butter",
                "ירקות", "פירות", "קטניות", "דגנים", "אגוזים", "שמנים צמחיים", "תבלינים טבעיים",
            ),
            IngredientStatus.OK,
            "Recognized plant-based ingredient",
        ),
    )

    private val notKosherRules = listOf(
        rule(
            listOf(
                "pork", "bacon", "ham", "lard", "animal fat", "tallow",
                "meat", "beef", "chicken", "beef extract", "chicken extract", "meat extract",
                "beef broth", "chicken broth", "meat broth", "beef stock", "chicken stock", "meat stock",
                "בשר", "שומן מן החי", "תמציות בשר",
            ),
            IngredientStatus.NOT_KOSHER,
            "Recognized animal-based ingredient",
            skipWhen = { ingredient ->
                val lower = ingredient.lowercase(Locale.ROOT)
                "yeast extract" in lower || "vanilla extract" in lower || "coffee extract" in lower
            },
        ),
        rule(
            listOf("gelatin", "gelatine", "ג'לטין", "גלטין"),
            IngredientStatus.NOT_KOSHER,
            "Gelatin is treated as non-kosher for this MVP",
        ),
        rule(
            listOf("shrimp", "prawn", "lobster", "crab", "squid", "calamari", "octopus", "shellfish", "שרימפס", "לובסטר", "סרטן", "קלמארי"),
            IngredientStatus.NOT_KOSHER,
            "Recognized seafood ingredient",
        ),
        rule(
            listOf("wine", "red wine", "white wine", "brandy", "cognac", "grappa", "wine vinegar", "יין", "ברנדי", "קוניאק", "חומץ יין"),
            IngredientStatus.NOT_KOSHER,
            "Recognized grape-based alcohol ingredient",
        ),
    )

    private val uncertainRules = listOf(
        rule(
            listOf("milk", "cheese", "whey", "casein", "caseinate", "cream", "חלב", "גבינה", "גבינות", "מי גבינה", "קזאין"),
            IngredientStatus.UNCERTAIN,
            "Dairy ingredient requires kosher-source check",
        ),
        rule(
            listOf("e471", "e-471", "e472", "e-472", "lecithin", "stabilizer", "stabilizers", "stabiliser", "stabilisers", "לציטין", "מייצב", "מייצבים"),
            IngredientStatus.UNCERTAIN,
            "Additive source is not clear",
        ),
        rule(
            listOf(
                "flavoring", "flavorings", "flavouring", "flavourings",
                "natural flavor", "natural flavors", "natural flavour", "natural flavours",
                "shortening", "emulsifier", "emulsifiers", "mono-diglycerides", "mono diglycerides",
                "mono- and diglycerides", "mono and diglycerides",
            ),
            IngredientStatus.UNCERTAIN,
            "Ingredient source is unclear",
            skipWhen = { ingredient -> "vegetable shortening" in ingredient.lowercase(Locale.ROOT) },
        ),
    )

    fun analyze(parsedIngredients: ParsedIngredients): KosherResult {
        val ingredients = parsedIngredients.items.ifEmpty { listOf(parsedIngredients.sectionText) }
        val assessments = ingredients.map(::assessIngredient)

        val verdict = when {
            assessments.any { it.status == IngredientStatus.NOT_KOSHER } -> Verdict.PROBLEM
            assessments.any { it.status == IngredientStatus.UNCERTAIN } -> Verdict.UNCERTAIN
            else -> Verdict.OK
        }

        return KosherResult(
            verdict = verdict,
            assessments = assessments,
        )
    }

    private fun assessIngredient(rawIngredient: String): IngredientAssessment {
        val ingredient = rawIngredient.trim().trim('.')

        firstMatchingRule(ingredient, notKosherRules)?.let { matched ->
            return IngredientAssessment(
                ingredient = ingredient,
                status = IngredientStatus.NOT_KOSHER,
                reason = matched.rule.reason,
                matchedKeyword = matched.keyword,
            )
        }

        firstMatchingRule(ingredient, uncertainRules)?.let { matched ->
            return IngredientAssessment(
                ingredient = ingredient,
                status = matched.rule.status,
                reason = matched.rule.reason,
                matchedKeyword = matched.keyword,
            )
        }

        firstMatchingRule(ingredient, okRules)?.let { matched ->
            return IngredientAssessment(
                ingredient = ingredient,
                status = IngredientStatus.OK,
                reason = matched.rule.reason,
                matchedKeyword = matched.keyword,
            )
        }

        return IngredientAssessment(
            ingredient = ingredient,
            status = IngredientStatus.UNCERTAIN,
            reason = "Ingredient not recognized by current rules",
            matchedKeyword = null,
        )
    }

    private data class MatchedRule(
        val rule: Rule,
        val keyword: String,
    )

    private fun firstMatchingRule(
        ingredient: String,
        rules: List<Rule>,
    ): MatchedRule? {
        val normalized = ingredient.lowercase(Locale.ROOT)
        for (rule in rules) {
            if (rule.skipWhen?.invoke(ingredient) == true) {
                continue
            }
            val keyword = rule.keywords.firstOrNull { candidate ->
                normalized.contains(candidate.lowercase(Locale.ROOT))
            }
            if (keyword != null) {
                return MatchedRule(rule = rule, keyword = keyword)
            }
        }
        return null
    }

    private fun rule(
        keywords: List<String>,
        status: IngredientStatus,
        reason: String,
        skipWhen: ((String) -> Boolean)? = null,
    ): Rule {
        return Rule(
            keywords = keywords,
            status = status,
            reason = reason,
            skipWhen = skipWhen,
        )
    }
}
