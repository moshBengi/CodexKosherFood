package com.example.codexkosherfood.domain.model

data class AiReviewRequest(
    val originalName: String = "",
    val normalizedName: String = "",
    val ingredientListText: String = "",
    val currentReason: String = "",
)

data class AiIngredientReview(
    val originalName: String = "",
    val normalizedName: String = "",
    val hebrewTranslation: String = "",
    val recommendation: IngredientStatus,
    val reason: String = "",
    val confidence: Double? = null,
)

data class AiReviewBatchResult(
    val reviews: List<AiIngredientReview>,
    val message: String? = null,
)
