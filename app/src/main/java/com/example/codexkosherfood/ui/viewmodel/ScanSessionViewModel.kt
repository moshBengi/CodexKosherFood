package com.example.codexkosherfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexkosherfood.data.repository.AnalysisOutput
import com.example.codexkosherfood.data.repository.ScanRepository
import com.example.codexkosherfood.domain.model.AiIngredientReview
import com.example.codexkosherfood.ocr.OcrScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanSessionUiState(
    val recognizedText: String = "",
    val editedText: String = "",
    val lowConfidenceHints: List<String> = emptyList(),
    val analysisOutput: AnalysisOutput? = null,
    val aiReviews: List<AiIngredientReview> = emptyList(),
    val isAiReviewLoading: Boolean = false,
    val aiReviewMessage: String? = null,
    val errorMessage: String? = null,
)

class ScanSessionViewModel(
    private val repository: ScanRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanSessionUiState())
    val uiState: StateFlow<ScanSessionUiState> = _uiState.asStateFlow()

    fun reset() {
        _uiState.value = ScanSessionUiState()
    }

    fun startManualEntry() {
        _uiState.value = ScanSessionUiState()
    }

    fun onOcrResult(result: OcrScanResult) {
        _uiState.update {
            it.copy(
                recognizedText = result.fullText.trim(),
                editedText = result.fullText.trim(),
                lowConfidenceHints = result.words
                    .filter { word -> word.lowConfidenceHint }
                    .map { word -> word.text }
                    .distinct()
                    .take(12),
                analysisOutput = null,
                aiReviews = emptyList(),
                isAiReviewLoading = false,
                aiReviewMessage = null,
                errorMessage = null,
            )
        }
    }

    fun updateEditedText(text: String) {
        _uiState.update {
            it.copy(
                editedText = text,
                analysisOutput = null,
                aiReviews = emptyList(),
                isAiReviewLoading = false,
                aiReviewMessage = null,
                errorMessage = null,
            )
        }
    }

    fun analyze() {
        val input = _uiState.value.editedText.trim()
        if (input.isBlank()) {
            _uiState.update { it.copy(errorMessage = "No text to analyze") }
            return
        }

        _uiState.update {
            it.copy(
                analysisOutput = repository.analyzeText(input),
                aiReviews = emptyList(),
                isAiReviewLoading = false,
                aiReviewMessage = null,
                errorMessage = null,
            )
        }
    }

    fun reviewUncertainIngredients() {
        val analysis = _uiState.value.analysisOutput
        if (analysis == null) {
            _uiState.update { it.copy(aiReviewMessage = "Run analysis first.") }
            return
        }
        if (analysis.result.uncertainItems.isEmpty()) {
            _uiState.update { it.copy(aiReviewMessage = "There are no uncertain ingredients to review.") }
            return
        }

        _uiState.update {
            it.copy(
                isAiReviewLoading = true,
                aiReviewMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching { repository.reviewUncertainIngredients(analysis) }
                .onSuccess { batch ->
                    _uiState.update {
                        it.copy(
                            aiReviews = batch.reviews,
                            isAiReviewLoading = false,
                            aiReviewMessage = batch.message,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isAiReviewLoading = false,
                            aiReviewMessage = error.message ?: "AI review failed.",
                        )
                    }
                }
        }
    }

}
