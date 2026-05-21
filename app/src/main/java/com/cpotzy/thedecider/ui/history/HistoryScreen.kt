package com.cpotzy.thedecider.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.CompletionWithTitle
import com.cpotzy.thedecider.data.db.entities.CompletionType
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class HistoryFilter(val label: String) { ALL("All"), DONE("Done"), SKIPPED("Skipped") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    completionDao: CompletionDao,
    onBack: () -> Unit,
) {
    var rows by remember { mutableStateOf<List<CompletionWithTitle>>(emptyList()) }
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch { rows = completionDao.recentWithTitle() }
    }

    val visible = remember(rows, filter) {
        when (filter) {
            HistoryFilter.ALL -> rows
            HistoryFilter.DONE -> rows.filter { it.type == CompletionType.DONE }
            HistoryFilter.SKIPPED -> rows.filter { it.type == CompletionType.SKIPPED_PRESSURE_KEPT }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HistoryFilter.values().forEach { f ->
                    FilterChip(
                        selected = f == filter,
                        onClick = { filter = f },
                        label = { Text(f.label) },
                    )
                }
            }
            if (visible.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (rows.isEmpty()) "No history yet — go finish a task."
                        else "Nothing matches this filter.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visible, key = { it.id }) { row ->
                        HistoryRow(row)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(row: CompletionWithTitle) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    row.taskTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    formatWhen(row.completedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        if (row.type == CompletionType.DONE) "Done" else "Skipped",
                    )
                },
            )
        }
    }
}

private val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val dayOfWeekFmt = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
private val dateFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

private fun formatWhen(instant: Instant): String {
    val zone = ZoneId.systemDefault()
    val local = instant.atZone(zone)
    val today = LocalDate.now(zone)
    val rowDate = local.toLocalDate()
    val daysAgo = Duration.between(rowDate.atStartOfDay(zone), today.atStartOfDay(zone)).toDays()
    val time = local.format(timeFmt)
    return when {
        daysAgo == 0L -> "Today, $time"
        daysAgo == 1L -> "Yesterday, $time"
        daysAgo in 2L..6L -> "${local.format(dayOfWeekFmt)}, $time"
        else -> "${local.format(dateFmt)}, $time"
    }
}
