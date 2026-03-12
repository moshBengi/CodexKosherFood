package com.example.codexkosherfood.ocr

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TextRecognizerManager {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(imageProxy: ImageProxy): OcrScanResult {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return OcrScanResult(fullText = "", words = emptyList())
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        return try {
            process(image)
        } finally {
            imageProxy.close()
        }
    }

    suspend fun recognize(bitmap: Bitmap, rotationDegrees: Int = 0): OcrScanResult {
        return process(InputImage.fromBitmap(bitmap, rotationDegrees))
    }

    fun close() {
        recognizer.close()
    }

    private suspend fun process(inputImage: InputImage): OcrScanResult {
        return suspendCancellableCoroutine { continuation ->
            recognizer
                .process(inputImage)
                .addOnSuccessListener { text ->
                    continuation.resume(
                        OcrScanResult(
                            fullText = text.text,
                            words = text.textBlocks
                                .flatMap { block -> block.lines }
                                .flatMap { line -> line.elements }
                                .map { element ->
                                    OcrWord(
                                        text = element.text,
                                        bounds = element.boundingBox,
                                        lowConfidenceHint = looksUncertain(element.text),
                                    )
                                },
                        ),
                    )
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }

    private fun looksUncertain(token: String): Boolean {
        val punctuationCount = token.count { !it.isLetterOrDigit() }
        val digitCount = token.count { it.isDigit() }
        return token.length == 1 || punctuationCount >= 2 || digitCount > token.length / 2
    }
}
