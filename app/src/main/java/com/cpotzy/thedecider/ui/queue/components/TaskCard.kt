package com.cpotzy.thedecider.ui.queue.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.domain.model.PressureTier
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.ui.theme.Neutral
import com.cpotzy.thedecider.ui.theme.PressureRed
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun TaskCard(
    task: Task,
    tier: PressureTier,
    now: Instant,
    modifier: Modifier = Modifier,
) {
    val edgeColor = when (tier) {
        PressureTier.OVERDUE -> PressureRed
        PressureTier.IN_WINDOW, PressureTier.ANYTIME -> Neutral
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(edgeColor),
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Text(task.title, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(task.duration.name.lowercase()) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Neutral.copy(alpha = 0.4f)))
                    AssistChip(onClick = {}, label = { Text("${task.energy.name.lowercase()} energy") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Neutral.copy(alpha = 0.4f)))
                }
                val lastDone = task.lastDoneAt
                if (lastDone != null) {
                    Spacer(Modifier.height(20.dp))
                    val daysAgo = ChronoUnit.DAYS.between(lastDone, now).coerceAtLeast(0)
                    Text(
                        "last done $daysAgo day${if (daysAgo == 1L) "" else "s"} ago",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
