package com.cpotzy.thedecider.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.SnoozeDao
import com.cpotzy.thedecider.work.NudgeSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    completionDao: CompletionDao,
    snoozeDao: SnoozeDao,
) {
    val context = LocalContext.current
    val settings = remember { NudgeSettings(context) }
    val scope = rememberCoroutineScope()
    var pendingReset by remember { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }

    var windowStart by remember { mutableStateOf(settings.windowStartHour) }
    var windowEnd by remember { mutableStateOf(settings.windowEndHour) }
    var maxPerDay by remember { mutableStateOf(settings.maxPerDay) }
    var minGap by remember { mutableStateOf(settings.minGapMinutes) }
    var quietAfterOpen by remember { mutableStateOf(settings.quietAfterOpenMinutes) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("Nudges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Stepper(
                label = "Window start",
                value = windowStart,
                onChange = { windowStart = it; settings.windowStartHour = it },
                min = 0,
                max = (windowEnd - 1).coerceAtLeast(0),
                step = 1,
                suffix = ":00",
            )
            Stepper(
                label = "Window end",
                value = windowEnd,
                onChange = { windowEnd = it; settings.windowEndHour = it },
                min = (windowStart + 1).coerceAtMost(24),
                max = 24,
                step = 1,
                suffix = ":00",
            )
            Stepper(
                label = "Max nudges per day",
                value = maxPerDay,
                onChange = { maxPerDay = it; settings.maxPerDay = it },
                min = 0,
                max = 10,
                step = 1,
            )
            Stepper(
                label = "Minimum gap between nudges",
                value = minGap,
                onChange = { minGap = it; settings.minGapMinutes = it },
                min = 0,
                max = 240,
                step = 15,
                suffix = " min",
            )
            Stepper(
                label = "Quiet after opening app",
                value = quietAfterOpen,
                onChange = { quietAfterOpen = it; settings.quietAfterOpenMinutes = it },
                min = 0,
                max = 480,
                step = 30,
                suffix = " min",
            )

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {
                settings.resetToDefaults()
                windowStart = settings.windowStartHour
                windowEnd = settings.windowEndHour
                maxPerDay = settings.maxPerDay
                minGap = settings.minGapMinutes
                quietAfterOpen = settings.quietAfterOpenMinutes
            }) { Text("Reset to defaults") }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Clear all completion + snooze history so every task becomes due again. Tasks themselves are kept.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            FilledTonalButton(onClick = { pendingReset = true }) {
                Text("Clear history")
            }
        }
    }

    if (pendingReset) {
        AlertDialog(
            onDismissRequest = { pendingReset = false },
            title = { Text("Clear history?") },
            text = { Text("This deletes every completion and snooze. Your tasks and steps stay. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingReset = false
                    scope.launch {
                        completionDao.deleteAll()
                        snoozeDao.deleteAll()
                        snackbarHost.showSnackbar("History cleared")
                    }
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { pendingReset = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun Stepper(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    min: Int,
    max: Int,
    step: Int,
    suffix: String = "",
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = { onChange((value - step).coerceAtLeast(min)) },
                enabled = value > min,
            ) { Text("−") }
            Spacer(Modifier.width(16.dp))
            Text(
                "$value$suffix",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            FilledTonalButton(
                onClick = { onChange((value + step).coerceAtMost(max)) },
                enabled = value < max,
            ) { Text("+") }
        }
    }
}
