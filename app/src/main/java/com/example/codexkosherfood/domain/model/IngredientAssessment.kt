package com.example.codexkosherfood.domain.model

enum class IngredientStatus {
    OK,
    NOT_KOSHER,
    UNCERTAIN,
}

data class IngredientAssessment(
    val ingredient: String,
    val status: IngredientStatus,
    val reason: String,
    val matchedKeyword: String? = null,
)
