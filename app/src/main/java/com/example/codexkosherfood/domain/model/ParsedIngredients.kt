package com.example.codexkosherfood.domain.model

data class ParsedIngredients(
    val sourceText: String,
    val sectionText: String,
    val items: List<String>,
    val header: String?,
)
