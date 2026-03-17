package com.example.codexkosherfood.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.codexkosherfood.domain.model.Verdict
import com.example.codexkosherfood.ui.components.AppHeroCard
import com.example.codexkosherfood.ui.components.AppPage
import com.example.codexkosherfood.ui.components.AppSectionCard
import com.example.codexkosherfood.ui.viewmodel.ScanSessionUiState

@Composable
fun ResultScreen(
    uiState: ScanSessionUiState,
    onBack: () -> Unit,
    onAiReview: (() -> Unit)?,
    onCheckAnotherIngredient: (() -> Unit)?,
) {
    val analysis = uiState.analysisOutput
    val result = analysis?.result ?: KosherResult(Verdict.OK, emptyList())

    AppPage {
        AppHeroCard(
            title = "תוצאת הבדיקה",
            subtitle = "הסיווג נקבע לפי הכללים המקומיים של האפליקציה, עם פירוט לכל רכיב.",
        )

        AppSectionCard(
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("פסק כללי", style = MaterialTheme.typography.titleLarge)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = verdictContainerColor(result.verdict),
                ),
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("סטטוס", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = verdictLabel(result.verdict),
                        style = MaterialTheme.typography.headlineMedium,
                        color = verdictColor(result.verdict),
                    )
                }
            }
        }

        AppSectionCard(
            modifier = Modifier.padding(top = 14.dp),
        ) {
            Text("רכיבים שזוהו", style = MaterialTheme.typography.titleLarge)
            Text(
                text = analysis?.parsedIngredients?.sectionText.orEmpty().ifBlank { uiState.editedText },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        AppSectionCard(
            modifier = Modifier.padding(top = 14.dp),
        ) {
            Text("ניתוח רכיבים", style = MaterialTheme.typography.titleLarge)
            if (result.assessments.isEmpty()) {
                Text(
                    text = "לא זוהו רכיבים לניתוח.",
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    result.assessments.forEach { assessment ->
                        AssessmentCard(assessment)
                    }
                }
            }
        }

        if (result.uncertainItems.isNotEmpty()) {
            AppSectionCard(
                modifier = Modifier.padding(top = 14.dp),
            ) {
                Text("בדיקת AI לרכיבים לא ודאיים", style = MaterialTheme.typography.titleLarge)

                if (uiState.isAiReviewLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.aiReviewMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                if (uiState.aiReviews.isEmpty()) {
                    Text(
                        text = "עדיין אין תוצאות AI לרכיבים הלא ודאיים.",
                        modifier = Modifier.padding(top = 12.dp),
                    )
                } else {
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        uiState.aiReviews.forEach { review ->
                            AiReviewCard(review)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            onAiReview?.let {
                OutlinedButton(
                    onClick = it,
                    enabled = result.uncertainItems.isNotEmpty() && !uiState.isAiReviewLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                ) {
                    Text("בדיקת AI")
                }
            }
            onCheckAnotherIngredient?.let {
                Button(
                    onClick = it,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                ) {
                    Text("בדוק רכיב נוסף")
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(54.dp),
        ) {
            Text("חזרה")
        }
    }
}

@Composable
private fun AssessmentCard(assessment: IngredientAssessment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusContainerColor(assessment.status),
        ),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(assessment.originalName, style = MaterialTheme.typography.titleMedium)
            Text("סטטוס: ${statusLabel(assessment.status)}")
            Text("סיבה: ${assessment.reason}")
            Text("זיהוי: ${assessment.normalizedName}")
            assessment.matchedKeyword?.let { Text("התאמה לפי: $it") }
        }
    }
}

@Composable
private fun AiReviewCard(review: AiIngredientReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(review.originalName, style = MaterialTheme.typography.titleMedium)
            Text("תרגום: ${review.hebrewTranslation}")
            Text("זיהוי: ${review.normalizedName}")
            Text("המלצת AI: ${statusLabel(review.recommendation)}")
            Text("הסבר: ${review.reason}")
            review.confidence?.let { Text("ביטחון: ${"%.2f".format(it)}") }
        }
    }
}

private fun statusLabel(status: IngredientStatus): String {
    return when (status) {
        IngredientStatus.OK -> "OK"
        IngredientStatus.NOT_KOSHER -> "NOT_KOSHER"
        IngredientStatus.UNCERTAIN -> "UNCERTAIN"
    }
}

private fun verdictLabel(verdict: Verdict): String {
    return when (verdict) {
        Verdict.OK -> "OK"
        Verdict.PROBLEM -> "NOT_KOSHER"
        Verdict.UNCERTAIN -> "UNCERTAIN"
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

@Composable
private fun verdictContainerColor(verdict: Verdict): Color {
    return when (verdict) {
        Verdict.OK -> MaterialTheme.colorScheme.primaryContainer
        Verdict.PROBLEM -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        Verdict.UNCERTAIN -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
    }
}

@Composable
private fun statusContainerColor(status: IngredientStatus): Color {
    return when (status) {
        IngredientStatus.OK -> MaterialTheme.colorScheme.primaryContainer
        IngredientStatus.NOT_KOSHER -> MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
        IngredientStatus.UNCERTAIN -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
    }
}
