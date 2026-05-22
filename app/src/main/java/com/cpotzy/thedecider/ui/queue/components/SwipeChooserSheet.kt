package com.cpotzy.thedecider.ui.queue.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.ui.queue.SnoozeKindChoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeChooserSheet(
    onChoose: (SnoozeKindChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Not now — when?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                "Pressure keeps building if you skip.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(4.dp))
            ChooserButton(
                glyph = "⏳",
                title = "Later today",
                subtitle = "Back in a few hours",
            ) { onChoose(SnoozeKindChoice.LATER_TODAY) }
            ChooserButton(
                glyph = "🌅",
                title = "Tomorrow",
                subtitle = "Back tomorrow morning",
            ) { onChoose(SnoozeKindChoice.TOMORROW) }
            ChooserButton(
                glyph = "⏭",
                title = "Skip this cycle",
                subtitle = "Logged as skipped — pressure stays",
            ) { onChoose(SnoozeKindChoice.SKIP_CYCLE) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChooserButton(
    glyph: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(glyph, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}
