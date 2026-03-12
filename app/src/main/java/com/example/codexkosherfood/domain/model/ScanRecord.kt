package com.example.codexkosherfood.domain.model

data class ScanRecord(
    val id: Long,
    val createdAt: Long,
    val verdict: Verdict,
    val recognizedText: String,
    val editedText: String,
    val parsedSection: String,
    val result: KosherResult,
    val aiReviews: List<AiIngredientReview>,
)
