package com.cpotzy.thedecider.ui.queue.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration as DurationTag
import com.cpotzy.thedecider.domain.model.PressureTier
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.ui.theme.OnPressureRed
import com.cpotzy.thedecider.ui.theme.PressureRedContainer
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun TaskCard(
    task: Task,
    tier: PressureTier,
    now: Instant,
    modifier: Modifier = Modifier,
) {
    val overdue = tier == PressureTier.OVERDUE
    val containerColor = if (overdue)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface
    val onContainer = if (overdue)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp)),
        color = containerColor,
        shadowElevation = if (overdue) 6.dp else 3.dp,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // header row: cadence + overdue/due badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                CadenceTag(task.cadence, onContainer)
                Spacer(Modifier.weight(1f))
                LastDoneBadge(task, now, overdue)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                task.title,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                color = onContainer,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(durationLabel(task.duration), onContainer)
                MetaPill(energyLabel(task.energy), onContainer)
            }
        }
    }
}

@Composable
private fun CadenceTag(cadence: Cadence, onContainer: androidx.compose.ui.graphics.Color) {
    val label = when (cadence) {
        Cadence.DAILY -> "daily"
        Cadence.BIDAILY -> "every 2 days"
        Cadence.WEEKLY -> "weekly"
        Cadence.BIWEEKLY -> "biweekly"
        Cadence.MONTHLY -> "monthly"
        Cadence.BIMONTHLY -> "bimonthly"
        Cadence.ANYTIME -> "anytime"
        Cadence.ONEOFF -> "one-off"
    }
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = onContainer.copy(alpha = 0.55f),
    )
}

@Composable
private fun LastDoneBadge(task: Task, now: Instant, overdue: Boolean) {
    val zone = ZoneId.systemDefault()
    val cadenceDays = task.cadence.cadenceDays
    val lastDone = task.lastDoneAt

    if (overdue && cadenceDays != null) {
        val ref = lastDone ?: task.createdAt
        val daysSince = ChronoUnit.DAYS.between(ref.atZone(zone).toLocalDate(), now.atZone(zone).toLocalDate())
        val daysLate = (daysSince - cadenceDays).coerceAtLeast(0)
        val text = when {
            daysLate <= 0 -> "due now"
            daysLate == 1L -> "1 day overdue"
            else -> "$daysLate days overdue"
        }
        Surface(
            color = PressureRedContainer,
            shape = RoundedCornerShape(50),
        ) {
            Text(
                text,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = OnPressureRed,
            )
        }
        return
    }

    if (lastDone == null) {
        Text(
            "never done",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    } else {
        val daysAgo = ChronoUnit.DAYS.between(
            lastDone.atZone(zone).toLocalDate(),
            now.atZone(zone).toLocalDate(),
        ).coerceAtLeast(0)
        val text = when (daysAgo) {
            0L -> "done today"
            1L -> "1 day ago"
            else -> "$daysAgo days ago"
        }
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}

@Composable
private fun MetaPill(text: String, onContainer: androidx.compose.ui.graphics.Color) {
    Surface(
        color = onContainer.copy(alpha = 0.08f),
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = onContainer.copy(alpha = 0.85f),
        )
    }
}

private fun durationLabel(d: DurationTag): String = when (d) {
    DurationTag.QUICK -> "≤ 5 min"
    DurationTag.SHORT -> "≤ 15 min"
    DurationTag.MEDIUM -> "≤ 30 min"
    DurationTag.LONG -> "30+ min"
}

private fun energyLabel(e: com.cpotzy.thedecider.domain.model.Energy): String = when (e) {
    com.cpotzy.thedecider.domain.model.Energy.LOW -> "low energy"
    com.cpotzy.thedecider.domain.model.Energy.MEDIUM -> "medium energy"
    com.cpotzy.thedecider.domain.model.Energy.HIGH -> "high energy"
}
