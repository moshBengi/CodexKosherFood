package com.example.codexkosherfood.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codexkosherfood.domain.model.ScanRecord
import com.example.codexkosherfood.domain.model.Verdict
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    items: List<ScanRecord>,
    onBack: () -> Unit,
    onItemClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("היסטוריית סריקות", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onBack) {
            Text("חזרה")
        }

        if (items.isEmpty()) {
            Text("עדיין לא נשמרו סריקות.")
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { it.id }) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(record.id) },
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(formatDate(record.createdAt), style = MaterialTheme.typography.titleMedium)
                            VerdictChip(record.verdict)
                        }
                        Text("OK: ${record.result.okItems.size} | Uncertain: ${record.result.uncertainItems.size} | Not kosher: ${record.result.notKosherItems.size}")
                        Text(record.parsedSection.ifBlank { record.editedText }.take(180))
                    }
                }
            }
        }
    }
}

@Composable
private fun VerdictChip(verdict: Verdict) {
    val colors = when (verdict) {
        Verdict.OK -> AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        Verdict.PROBLEM -> AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        Verdict.UNCERTAIN -> AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    }
    AssistChip(
        onClick = {},
        label = { Text(verdict.name) },
        colors = colors,
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}
