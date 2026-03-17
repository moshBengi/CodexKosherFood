package com.example.codexkosherfood.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codexkosherfood.ui.components.AppPage
import com.example.codexkosherfood.ui.components.AppSectionCard

@Composable
fun HomeScreen(
    onNewScan: () -> Unit,
    onManualCheck: () -> Unit,
    onGuide: () -> Unit,
) {
    val showScanOptions = false

    AppPage {
        AppSectionCard(
            modifier = Modifier,
        ) {
            Text(
                text = "מה אפשר לעשות עכשיו",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "הגרסה הנוכחית מתמקדת בבדיקה ידנית של רכיבים ובהצגת הסבר מסודר לכל תוצאה.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 18.dp),
            )

            Button(
                onClick = onManualCheck,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text("בדוק רכיב")
            }

            OutlinedButton(
                onClick = onGuide,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(54.dp),
            ) {
                Text("הנחיות כשרות לחול")
            }

            if (showScanOptions) {
                OutlinedButton(
                    onClick = onNewScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(54.dp),
                ) {
                    Text("סריקה חדשה")
                }
            }
        }
    }
}
