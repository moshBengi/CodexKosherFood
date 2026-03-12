package com.example.codexkosherfood.ocr

import android.graphics.Rect

data class OcrWord(
    val text: String,
    val bounds: Rect?,
    val lowConfidenceHint: Boolean,
)

data class OcrScanResult(
    val fullText: String,
    val words: List<OcrWord>,
)
