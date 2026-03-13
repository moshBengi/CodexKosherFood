package com.example.codexkosherfood.domain.model

enum class IngredientStatus {
    OK,
    NOT_KOSHER,
    UNCERTAIN,
}

data class IngredientAssessment(
    val originalName: String = "",
    val normalizedName: String = "",
    val status: IngredientStatus,
    val reason: String = "",
    val matchedKeyword: String? = null,
)
