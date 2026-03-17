package com.example.codexkosherfood.data.repository

import com.example.codexkosherfood.data.ai.AiIngredientReviewer
import com.example.codexkosherfood.domain.model.AiReviewBatchResult
import com.example.codexkosherfood.domain.model.AiReviewRequest
import com.example.codexkosherfood.domain.model.KosherResult
import com.example.codexkosherfood.domain.model.ParsedIngredients
import com.example.codexkosherfood.domain.parser.IngredientParser
import com.example.codexkosherfood.domain.rules.KosherRulesEngine

data class AnalysisOutput(
    val parsedIngredients: ParsedIngredients,
    val result: KosherResult,
)

class ScanRepository(
    private val ingredientParser: IngredientParser,
    private val rulesEngine: KosherRulesEngine,
    private val aiIngredientReviewer: AiIngredientReviewer,
) {
    fun analyzeText(text: String): AnalysisOutput {
        val parsed = ingredientParser.parse(text)
        val result = rulesEngine.analyze(parsed)
        return AnalysisOutput(parsedIngredients = parsed, result = result)
    }

    suspend fun reviewUncertainIngredients(
        analysis: AnalysisOutput,
    ): AiReviewBatchResult {
        val requests = analysis.result.uncertainItems
            .distinctBy { assessment -> assessment.normalizedName }
            .map { assessment ->
            AiReviewRequest(
                originalName = assessment.originalName,
                normalizedName = assessment.normalizedName,
                ingredientListText = analysis.parsedIngredients.sectionText,
                currentReason = assessment.reason,
            )
        }
        return aiIngredientReviewer.reviewUncertainIngredients(requests)
    }
}
