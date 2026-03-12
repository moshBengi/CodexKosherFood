package com.example.codexkosherfood.domain.model

data class AiReviewRequest(
    val ingredient: String,
    val ingredientListText: String,
    val currentReason: String,
)

data class AiIngredientReview(
    val ingredient: String,
    val hebrewTranslation: String,
    val normalizedName: String,
    val recommendation: IngredientStatus,
    val reason: String,
    val confidence: Double? = null,
)

data class AiReviewBatchResult(
    val reviews: List<AiIngredientReview>,
    val message: String? = null,
)
