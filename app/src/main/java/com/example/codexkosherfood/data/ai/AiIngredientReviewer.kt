package com.example.codexkosherfood.data.ai

import com.example.codexkosherfood.domain.model.AiReviewBatchResult
import com.example.codexkosherfood.domain.model.AiReviewRequest

interface AiIngredientReviewer {
    val isConfigured: Boolean

    suspend fun reviewUncertainIngredients(
        requests: List<AiReviewRequest>,
    ): AiReviewBatchResult
}
