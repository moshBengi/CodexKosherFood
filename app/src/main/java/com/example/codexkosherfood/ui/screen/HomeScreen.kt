package com.example.codexkosherfood.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNewScan: () -> Unit,
    onManualCheck: () -> Unit,
    onHistory: () -> Unit,
) {
    val showScanOptions = false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(text = "Kosher Scan MVP", style = MaterialTheme.typography.headlineMedium)
        Text("Local OCR for ingredient lists with deterministic kosher rules.")

        OutlinedButton(onClick = onManualCheck, modifier = Modifier.fillMaxWidth()) {
            Text("בדוק רכיב")
        }

        if (showScanOptions) {
            Button(onClick = onNewScan, modifier = Modifier.fillMaxWidth()) {
                Text("סריקה חדשה")
            }

            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) {
                Text("היסטוריה")
            }
        }
    }
}
