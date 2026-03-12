package com.example.codexkosherfood.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.codexkosherfood.camera.CameraCaptureManager
import com.example.codexkosherfood.camera.CameraSession
import com.example.codexkosherfood.ocr.OcrScanResult
import com.example.codexkosherfood.ocr.TextRecognizerManager
import com.example.codexkosherfood.util.BitmapLoader
import kotlinx.coroutines.launch

@Composable
fun CameraScanScreen(
    textRecognizerManager: TextRecognizerManager,
    onBack: () -> Unit,
    onScanResult: (OcrScanResult) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val previewView = remember { PreviewView(context) }
    val captureManager = remember(context) { CameraCaptureManager(context, textRecognizerManager) }

    var hasCameraPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var cameraSession by remember { mutableStateOf<CameraSession?>(null) }
    var flashEnabled by rememberSaveable { mutableStateOf(false) }
    var isBusy by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        coroutineScope.launch {
            isBusy = true
            errorMessage = null
            runCatching {
                val bitmap = BitmapLoader.load(context, uri)
                textRecognizerManager.recognize(bitmap)
            }.onSuccess(onScanResult)
                .onFailure { errorMessage = it.message ?: "שגיאת OCR" }
            isBusy = false
        }
    }

    LaunchedEffect(hasCameraPermission, lifecycleOwner) {
        if (hasCameraPermission) {
            runCatching { captureManager.bindPreview(lifecycleOwner, previewView) }
                .onSuccess { cameraSession = it }
                .onFailure { errorMessage = it.message ?: "לא ניתן לפתוח מצלמה" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "סריקת מרכיבים", style = MaterialTheme.typography.headlineSmall)
        Text(text = "צילום מוצר או בחירה מהגלריה. ה-OCR פועל מקומית על המכשיר.")

        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                factory = { previewView },
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("נדרשת הרשאת מצלמה לסריקה ישירה")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("אשר מצלמה")
                }
            }
        }

        errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        if (isBusy) {
            CircularProgressIndicator()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    val session = cameraSession ?: return@Button
                    coroutineScope.launch {
                        isBusy = true
                        errorMessage = null
                        runCatching { captureManager.captureAndRecognize(session) }
                            .onSuccess(onScanResult)
                            .onFailure { errorMessage = it.message ?: "צילום נכשל" }
                        isBusy = false
                    }
                },
                enabled = cameraSession != null && !isBusy,
                modifier = Modifier.weight(1f),
            ) {
                Text("צלם")
            }

            OutlinedButton(
                onClick = {
                    flashEnabled = !flashEnabled
                    cameraSession?.let { captureManager.setTorchEnabled(it, flashEnabled) }
                },
                enabled = cameraSession != null && !isBusy,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (flashEnabled) "פלאש כבוי" else "פלאש")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("בחר מהגלריה")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
            ) {
                Text("חזרה")
            }
        }
    }
}
