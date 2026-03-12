package com.example.codexkosherfood.data.ai

import com.example.codexkosherfood.domain.model.AiReviewBatchResult
import com.example.codexkosherfood.domain.model.AiReviewRequest

class DisabledAiIngredientReviewer : AiIngredientReviewer {
    override val isConfigured: Boolean = false

    override suspend fun reviewUncertainIngredients(
        requests: List<AiReviewRequest>,
    ): AiReviewBatchResult {
        return AiReviewBatchResult(
            reviews = emptyList(),
            message = if (requests.isEmpty()) {
                "There are no uncertain ingredients to review."
            } else {
                "AI review is not configured yet. The Kotlin side is ready for a future backend integration."
            },
        )
    }
}
