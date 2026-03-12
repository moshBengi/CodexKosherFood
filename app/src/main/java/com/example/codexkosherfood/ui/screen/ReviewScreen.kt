package com.example.codexkosherfood.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codexkosherfood.ui.viewmodel.ScanSessionUiState

@Composable
fun ReviewScreen(
    uiState: ScanSessionUiState,
    onBack: () -> Unit,
    onTextChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("בדיקת טקסט לפני ניתוח", style = MaterialTheme.typography.headlineSmall)
        Text("אפשר לתקן OCR ידנית לפני ניתוח הכללים.")

        OutlinedTextField(
            value = uiState.editedText,
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            label = { Text("טקסט מזוהה") },
            minLines = 8,
        )

        if (uiState.lowConfidenceHints.isNotEmpty()) {
            Text("מילים שכנראה דורשות בדיקה", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.lowConfidenceHints.forEach { hint ->
                    AssistChip(onClick = {}, label = { Text(hint) })
                }
            }
        }

        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onAnalyze,
            enabled = uiState.editedText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Analyze")
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("חזרה")
        }
    }
}
