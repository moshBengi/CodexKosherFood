package com.example.codexkosherfood.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codexkosherfood.domain.model.ScanRecord

@Composable
fun HistoryDetailScreen(
    record: ScanRecord?,
    onBack: () -> Unit,
) {
    if (record == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("רשומה לא נמצאה", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBack) {
                Text("חזרה")
            }
        }
        return
    }

    ResultScreen(
        record = record,
        onBack = onBack,
        onSave = null,
        onScanAgain = null,
    )
}
