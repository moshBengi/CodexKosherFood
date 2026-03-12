package com.example.codexkosherfood.domain.model

enum class Verdict {
    OK,
    PROBLEM,
    UNCERTAIN,
}

data class KosherResult(
    val verdict: Verdict,
    val assessments: List<IngredientAssessment>,
) {
    val okItems: List<IngredientAssessment>
        get() = assessments.filter { it.status == IngredientStatus.OK }

    val notKosherItems: List<IngredientAssessment>
        get() = assessments.filter { it.status == IngredientStatus.NOT_KOSHER }

    val uncertainItems: List<IngredientAssessment>
        get() = assessments.filter { it.status == IngredientStatus.UNCERTAIN }
}
