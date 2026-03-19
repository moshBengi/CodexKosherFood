package com.example.codexkosherfood.ui.screen
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codexkosherfood.ui.components.AppHeroCard
import com.example.codexkosherfood.ui.components.AppPage
import com.example.codexkosherfood.ui.components.AppSectionCard
import com.example.codexkosherfood.ui.viewmodel.ScanSessionUiState
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(
    uiState: ScanSessionUiState,
    onBack: () -> Unit,
    onTextChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
) {
    AppPage {
        AppHeroCard(
            title = "בדיקת רכיב",
            subtitle = "הזינו רשימת רכיבים או רכיב בודד, ותקבלו ניתוח מקומי עם הסבר ברור.",
        )

        AppSectionCard(
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("טקסט לבדיקה", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "אפשר להקליד, לערוך ולתקן את הטקסט לפני הניתוח.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
            )

            OutlinedTextField(
                value = uiState.editedText,
                onValueChange = onTextChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("רכיבים") },
                minLines = 8,
                shape = MaterialTheme.shapes.large,
            )

            if (uiState.lowConfidenceHints.isNotEmpty()) {
                Text(
                    text = "מילים שדורשות תשומת לב",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.lowConfidenceHints.forEach { hint ->
                        AssistChip(
                            onClick = {},
                            label = { Text(hint) },
                        )
                    }
                }
            }

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }

            Button(
                onClick = onAnalyze,
                enabled = uiState.editedText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .height(54.dp),
            ) {
                Text("נתח רכיב")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(54.dp),
            ) {
                Text("חזרה")
            }
        }
    }
}
