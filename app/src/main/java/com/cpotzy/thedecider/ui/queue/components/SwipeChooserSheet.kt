package com.cpotzy.thedecider.ui.queue.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.ui.queue.SnoozeKindChoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeChooserSheet(
    onChoose: (SnoozeKindChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Defer this one?", style = MaterialTheme.typography.titleLarge)
            ChooserButton("Later today", subtitle = "Comes back in a few hours") {
                onChoose(SnoozeKindChoice.LATER_TODAY)
            }
            ChooserButton("Tomorrow", subtitle = "Comes back tomorrow morning") {
                onChoose(SnoozeKindChoice.TOMORROW)
            }
            ChooserButton(
                "Skip this cycle",
                subtitle = "Logs as skipped — pressure keeps building",
            ) {
                onChoose(SnoozeKindChoice.SKIP_CYCLE)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChooserButton(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.labelSmall)
        }
    }
}
