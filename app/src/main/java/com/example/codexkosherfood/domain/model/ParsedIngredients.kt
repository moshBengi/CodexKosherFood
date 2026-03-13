package com.example.codexkosherfood.domain.model

data class ParsedIngredient(
    val originalName: String = "",
    val normalizedName: String = "",
)

data class ParsedIngredients(
    val sourceText: String,
    val sectionText: String,
    val items: List<ParsedIngredient>,
    val header: String?,
)
