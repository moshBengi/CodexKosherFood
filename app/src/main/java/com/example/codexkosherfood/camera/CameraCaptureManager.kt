package com.example.codexkosherfood.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.codexkosherfood.ocr.OcrScanResult
import com.example.codexkosherfood.ocr.TextRecognizerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CameraSession(
    val camera: Camera,
    val imageCapture: ImageCapture,
)

class CameraCaptureManager(
    private val context: Context,
    private val textRecognizerManager: TextRecognizerManager,
) {
    suspend fun bindPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    ): CameraSession {
        val provider = awaitCameraProvider()
        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        provider.unbindAll()
        val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
        return CameraSession(camera = camera, imageCapture = imageCapture)
    }

    fun setTorchEnabled(session: CameraSession, enabled: Boolean) {
        session.camera.cameraControl.enableTorch(enabled)
    }

    suspend fun captureAndRecognize(session: CameraSession): OcrScanResult {
        return suspendCancellableCoroutine { continuation ->
            session.imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                        CoroutineScope(Dispatchers.Main.immediate).launch {
                            runCatching { textRecognizerManager.recognize(image) }
                                .onSuccess { continuation.resume(it) }
                                .onFailure { continuation.resumeWithException(it) }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                },
            )
        }
    }

    private suspend fun awaitCameraProvider(): ProcessCameraProvider {
        val future = ProcessCameraProvider.getInstance(context)
        return suspendCancellableCoroutine { continuation ->
            future.addListener(
                { continuation.resume(future.get()) },
                ContextCompat.getMainExecutor(context),
            )
        }
    }
}
