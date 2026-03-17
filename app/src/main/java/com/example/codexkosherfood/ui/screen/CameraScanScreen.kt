package com.example.codexkosherfood.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
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
import com.example.codexkosherfood.ui.components.AppHeroCard
import com.example.codexkosherfood.ui.components.AppInsetPanel
import com.example.codexkosherfood.ui.components.AppPage
import com.example.codexkosherfood.ui.components.AppSectionCard
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
        if (uri == null) return@rememberLauncherForActivityResult
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

    AppPage(scrollable = true) {
        AppHeroCard(
            title = "סריקת רכיבים",
            subtitle = "המסך נשאר במערכת, אבל כרגע הזרימה הראשית מכוונת לבדיקה ידנית.",
        )

        AppSectionCard(
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("תצוגת מצלמה", style = MaterialTheme.typography.titleLarge)

            AppInsetPanel(modifier = Modifier.padding(top = 14.dp)) {
                if (hasCameraPermission) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        factory = { previewView },
                    )
                } else {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("נדרשת הרשאת מצלמה כדי להשתמש בסריקה.")
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.padding(top = 12.dp),
                        ) {
                            Text("אשר מצלמה")
                        }
                    }
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }

            if (isBusy) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = {
                        val session = cameraSession ?: return@Button
                        coroutineScope.launch {
                            isBusy = true
                            errorMessage = null
                            runCatching { captureManager.captureAndRecognize(session) }
                                .onSuccess(onScanResult)
                                .onFailure { errorMessage = it.message ?: "הצילום נכשל" }
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
                    Text(if (flashEnabled) "כבה פלאש" else "הפעל פלאש")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
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
}
