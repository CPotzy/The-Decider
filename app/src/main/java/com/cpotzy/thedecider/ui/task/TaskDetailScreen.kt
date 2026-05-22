package com.cpotzy.thedecider.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.data.db.entities.StepEntity
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.task?.title ?: "", maxLines = 2) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    if (state.steps.isNotEmpty()) {
                        TextButton(onClick = { viewModel.toggleFocusMode() }) {
                            Text(if (state.focusMode) "List" else "Focus")
                        }
                    }
                },
            )
        },
        bottomBar = {
            val allChecked = state.steps.isEmpty() ||
                state.steps.all { it.id in state.checkedStepIds }
            BottomAppBar {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { viewModel.finishTask() },
                    enabled = allChecked,
                    modifier = Modifier.padding(end = 16.dp),
                ) {
                    Text(if (allChecked) "Done with task" else "Check off all steps first")
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                state.task == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.steps.isEmpty() -> AtomicTaskView()
                state.focusMode -> FocusModeView(
                    state = state,
                    onNext = viewModel::nextFocusStep,
                    onPrev = viewModel::prevFocusStep,
                    onMarkDone = viewModel::markCurrentFocusDone,
                )
                else -> ChecklistView(
                    state = state,
                    onToggle = viewModel::toggleStep,
                )
            }
        }
    }
}

@Composable
private fun AtomicTaskView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Just do it.", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            "Tap \"Done with task\" when you're finished.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun ChecklistView(
    state: TaskDetailUiState,
    onToggle: (Long) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val done = state.steps.count { it.id in state.checkedStepIds }
    val total = state.steps.size
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(50),
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            Text(
                "$done of $total steps",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state.steps.forEach { step ->
            val checked = step.id in state.checkedStepIds
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle(step.id)
                },
                shape = RoundedCornerShape(12.dp),
                tonalElevation = if (checked) 0.dp else 2.dp,
                color = if (checked)
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = checked, onCheckedChange = { onToggle(step.id) })
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            step.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = if (checked) TextDecoration.LineThrough else null,
                            ),
                            color = if (checked)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        step.durationSeconds?.let { secs ->
                            Spacer(Modifier.height(2.dp))
                            Text(
                                formatDuration(secs),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun FocusModeView(
    state: TaskDetailUiState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onMarkDone: () -> Unit,
) {
    val current = state.steps.getOrNull(state.currentFocusIndex) ?: return
    val totalSteps = state.steps.size
    val checked = current.id in state.checkedStepIds

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Step ${state.currentFocusIndex + 1} of $totalSteps",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    current.content,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                current.durationSeconds?.let { secs ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Target: ${formatDuration(secs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        StepTimer(step = current)

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onMarkDone,
            enabled = !checked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (checked) "Step done ✓" else "Done with step")
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onPrev, enabled = state.currentFocusIndex > 0) {
                Text("← Previous")
            }
            TextButton(onClick = onNext, enabled = state.currentFocusIndex < totalSteps - 1) {
                Text("Next →")
            }
        }
    }
}

@Composable
private fun StepTimer(step: StepEntity) {
    val target = step.durationSeconds
    var running by remember(step.id) { mutableStateOf(false) }
    var elapsedMs by remember(step.id) { mutableStateOf(0L) }
    val targetMs = target?.let { it * 1000L }

    LaunchedEffect(running, step.id) {
        if (!running) return@LaunchedEffect
        val startedAt = System.currentTimeMillis() - elapsedMs
        while (running) {
            delay(100)
            elapsedMs = System.currentTimeMillis() - startedAt
        }
    }

    val remainingMs = targetMs?.let { (it - elapsedMs).coerceAtLeast(0L) }
    val overran = targetMs != null && elapsedMs > targetMs

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = when {
            !running && elapsedMs == 0L -> MaterialTheme.colorScheme.surfaceVariant
            overran -> Color(0xFFFFE2C7)
            else -> Color(0xFFE3F2D5)
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val display = when {
                running && remainingMs != null && !overran -> formatMs(remainingMs)
                running && overran && targetMs != null -> "+${formatMs(elapsedMs - targetMs)}"
                running -> formatMs(elapsedMs)
                target == null -> "No target — just press start"
                else -> "Target: ${formatDuration(target)}"
            }
            Text(
                display,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { running = !running }) {
                    Text(if (running) "Pause" else if (elapsedMs > 0L) "Resume" else "Start")
                }
                if (elapsedMs > 0L) {
                    OutlinedButton(onClick = { running = false; elapsedMs = 0L }) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String = when {
    seconds < 60 -> "≈ ${seconds}s"
    seconds < 3600 -> {
        val m = seconds / 60
        val s = seconds % 60
        if (s == 0) "≈ ${m} min" else "≈ ${m} min ${s}s"
    }
    else -> "≈ ${seconds / 3600}h ${seconds % 3600 / 60}m"
}

private fun formatMs(ms: Long): String {
    val totalSecs = ms / 1000
    val m = totalSecs / 60
    val s = totalSecs % 60
    return "%d:%02d".format(m, s)
}
