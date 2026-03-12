package com.example.codexkosherfood.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.codexkosherfood.domain.model.AiIngredientReview
import com.example.codexkosherfood.domain.model.IngredientAssessment
import com.example.codexkosherfood.domain.model.IngredientStatus
import com.example.codexkosherfood.domain.model.KosherResult
import com.example.codexkosherfood.domain.model.ScanRecord
import com.example.codexkosherfood.domain.model.Verdict
import com.example.codexkosherfood.ui.viewmodel.ScanSessionUiState

@Composable
fun ResultScreen(
    uiState: ScanSessionUiState,
    onBack: () -> Unit,
    onAiReview: (() -> Unit)?,
    onSave: (() -> Unit)?,
    onScanAgain: (() -> Unit)?,
) {
    val analysis = uiState.analysisOutput
    val result = analysis?.result ?: KosherResult(Verdict.OK, emptyList())

    ResultContent(
        title = "Analysis Result",
        editedText = uiState.editedText,
        parsedSection = analysis?.parsedIngredients?.sectionText.orEmpty(),
        result = result,
        aiReviews = uiState.aiReviews,
        isAiReviewLoading = uiState.isAiReviewLoading,
        aiReviewMessage = uiState.aiReviewMessage,
        savedMessage = uiState.savedScanId?.let { "Saved to history (#$it)" },
        onBack = onBack,
        onAiReview = onAiReview,
        onSave = onSave,
        onScanAgain = onScanAgain,
    )
}

@Composable
fun ResultScreen(
    record: ScanRecord,
    onBack: () -> Unit,
    onSave: (() -> Unit)?,
    onScanAgain: (() -> Unit)?,
) {
    ResultContent(
        title = "Saved Scan",
        editedText = record.editedText,
        parsedSection = record.parsedSection,
        result = record.result,
        aiReviews = record.aiReviews,
        isAiReviewLoading = false,
        aiReviewMessage = null,
        savedMessage = null,
        onBack = onBack,
        onAiReview = null,
        onSave = onSave,
        onScanAgain = onScanAgain,
    )
}

@Composable
private fun ResultContent(
    title: String,
    editedText: String,
    parsedSection: String,
    result: KosherResult,
    aiReviews: List<AiIngredientReview>,
    isAiReviewLoading: Boolean,
    aiReviewMessage: String?,
    savedMessage: String?,
    onBack: () -> Unit,
    onAiReview: (() -> Unit)?,
    onSave: (() -> Unit)?,
    onScanAgain: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Verdict", style = MaterialTheme.typography.titleMedium)
                Text(
                    result.verdict.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = verdictColor(result.verdict),
                )
            }
        }

        if (savedMessage != null) {
            Text(savedMessage, color = MaterialTheme.colorScheme.primary)
        }

        Text("Parsed ingredients", style = MaterialTheme.typography.titleMedium)
        Text(parsedSection.ifBlank { editedText })

        Text("Ingredient analysis", style = MaterialTheme.typography.titleMedium)
        if (result.assessments.isEmpty()) {
            Text("No ingredients were parsed.")
        } else {
            result.assessments.forEach { assessment ->
                AssessmentCard(assessment)
            }
        }

        if (result.uncertainItems.isNotEmpty()) {
            Text("AI review for uncertain ingredients", style = MaterialTheme.typography.titleMedium)

            if (isAiReviewLoading) {
                CircularProgressIndicator()
            }

            aiReviewMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.tertiary)
            }

            if (aiReviews.isEmpty()) {
                Text("No AI review results yet.")
            } else {
                aiReviews.forEach { review ->
                    AiReviewCard(review)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            onAiReview?.let {
                OutlinedButton(
                    onClick = it,
                    enabled = result.uncertainItems.isNotEmpty() && !isAiReviewLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Review Uncertain")
                }
            }
            onSave?.let {
                Button(onClick = it, modifier = Modifier.weight(1f)) {
                    Text("Save to History")
                }
            }
            onScanAgain?.let {
                OutlinedButton(onClick = it, modifier = Modifier.weight(1f)) {
                    Text("Scan Again")
                }
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun AssessmentCard(assessment: IngredientAssessment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(assessment.ingredient, style = MaterialTheme.typography.titleSmall)
            Text("Status: ${statusLabel(assessment.status)}")
            Text("Reason: ${assessment.reason}")
            assessment.matchedKeyword?.let { Text("Matched by: $it") }
        }
    }
}

@Composable
private fun AiReviewCard(review: AiIngredientReview) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(review.ingredient, style = MaterialTheme.typography.titleSmall)
            Text("Hebrew: ${review.hebrewTranslation}")
            Text("Normalized: ${review.normalizedName}")
            Text("AI Recommendation: ${statusLabel(review.recommendation)}")
            Text("AI Reason: ${review.reason}")
            review.confidence?.let { Text("Confidence: ${"%.2f".format(it)}") }
        }
    }
}

private fun statusLabel(status: IngredientStatus): String {
    return when (status) {
        IngredientStatus.OK -> "OK"
        IngredientStatus.NOT_KOSHER -> "NOT KOSHER"
        IngredientStatus.UNCERTAIN -> "UNCERTAIN"
    }
}

@Composable
private fun verdictColor(verdict: Verdict): Color {
    return when (verdict) {
        Verdict.OK -> MaterialTheme.colorScheme.primary
        Verdict.PROBLEM -> MaterialTheme.colorScheme.error
        Verdict.UNCERTAIN -> MaterialTheme.colorScheme.tertiary
    }
}
