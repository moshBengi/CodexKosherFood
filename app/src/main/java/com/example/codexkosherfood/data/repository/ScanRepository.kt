package com.example.codexkosherfood.data.repository

import com.example.codexkosherfood.data.ai.AiIngredientReviewer
import com.example.codexkosherfood.data.local.ScanHistoryDao
import com.example.codexkosherfood.data.local.ScanHistoryEntity
import com.example.codexkosherfood.domain.model.AiIngredientReview
import com.example.codexkosherfood.domain.model.AiReviewBatchResult
import com.example.codexkosherfood.domain.model.AiReviewRequest
import com.example.codexkosherfood.domain.model.KosherResult
import com.example.codexkosherfood.domain.model.ParsedIngredients
import com.example.codexkosherfood.domain.model.ScanRecord
import com.example.codexkosherfood.domain.model.Verdict
import com.example.codexkosherfood.domain.parser.IngredientParser
import com.example.codexkosherfood.domain.rules.KosherRulesEngine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AnalysisOutput(
    val parsedIngredients: ParsedIngredients,
    val result: KosherResult,
)

class ScanRepository(
    private val dao: ScanHistoryDao,
    private val ingredientParser: IngredientParser,
    private val rulesEngine: KosherRulesEngine,
    private val aiIngredientReviewer: AiIngredientReviewer,
    private val gson: Gson,
) {
    private val aiReviewListType = object : TypeToken<List<AiIngredientReview>>() {}.type

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

    suspend fun saveScan(
        recognizedText: String,
        editedText: String,
        analysis: AnalysisOutput,
        aiReviews: List<AiIngredientReview>,
    ): Long {
        return dao.insert(
            ScanHistoryEntity(
                createdAt = System.currentTimeMillis(),
                verdict = analysis.result.verdict.name,
                recognizedText = recognizedText,
                editedText = editedText,
                parsedSection = analysis.parsedIngredients.sectionText,
                resultJson = gson.toJson(analysis.result),
                aiReviewJson = gson.toJson(aiReviews),
            ),
        )
    }

    fun observeHistory(): Flow<List<ScanRecord>> {
        return dao.observeAll().map { items -> items.map(::toDomain) }
    }

    fun observeScan(id: Long): Flow<ScanRecord?> {
        return dao.observeById(id).map { entity -> entity?.let(::toDomain) }
    }

    private fun toDomain(entity: ScanHistoryEntity): ScanRecord {
        return ScanRecord(
            id = entity.id,
            createdAt = entity.createdAt,
            verdict = Verdict.valueOf(entity.verdict),
            recognizedText = entity.recognizedText,
            editedText = entity.editedText,
            parsedSection = entity.parsedSection,
            result = gson.fromJson(entity.resultJson, KosherResult::class.java)
                ?: KosherResult(verdict = Verdict.valueOf(entity.verdict), assessments = emptyList()),
            aiReviews = gson.fromJson(entity.aiReviewJson, aiReviewListType) ?: emptyList(),
        )
    }
}
