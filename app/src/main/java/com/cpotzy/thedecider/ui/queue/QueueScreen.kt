package com.cpotzy.thedecider.ui.queue

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.data.repo.TaskRepository
import com.cpotzy.thedecider.ui.quickadd.QuickAddSheet
import com.cpotzy.thedecider.ui.queue.components.ModeChipRow
import com.cpotzy.thedecider.ui.queue.components.SwipeChooserSheet
import com.cpotzy.thedecider.ui.queue.components.TaskCard
import java.time.Instant

@Composable
fun QueueScreen(
    viewModel: QueueViewModel,
    onAcceptTask: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenSettings: () -> Unit,
    taskRepository: TaskRepository,
    now: Instant = Instant.now(),
) {
    val state by viewModel.state.collectAsState()
    var offsetX by remember { mutableStateOf(0f) }
    var showChooser by remember { mutableStateOf(false) }
    var showQuickAdd by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 120.dp.toPx() }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "swipeOffset")

    val dragState = rememberDraggableState { delta -> offsetX += delta }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.onResume() }
    LaunchedEffect(Unit) {
        viewModel.celebrations.collect { msg ->
            snackbarHost.showSnackbar(msg)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp)) {
            // top bar — minimal nav, today counter on left
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TodayCountChip(state.doneTodayCount)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onOpenTasks) { Text("Tasks") }
                TextButton(onClick = onOpenHistory) { Text("History") }
                TextButton(onClick = onOpenSettings) { Text("Settings") }
            }

            val update = state.update
            if (update != null && !state.updateDismissed) {
                Spacer(Modifier.height(8.dp))
                UpdateBanner(
                    onDownload = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.releaseUrl))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        viewModel.dismissUpdateBanner()
                    },
                    onDismiss = { viewModel.dismissUpdateBanner() },
                )
            }

            Spacer(Modifier.height(16.dp))
            ModeChipRow(
                chips = state.modeChips,
                selected = state.mode,
                onSelect = { viewModel.setMode(it) },
            )
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Horizontal,
                        enabled = state.task != null,
                        onDragStopped = {
                            val currentId = viewModel.currentTaskId()
                            when {
                                currentId == null -> offsetX = 0f
                                offsetX > swipeThresholdPx -> {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAcceptTask(currentId)
                                    offsetX = 0f
                                }
                                offsetX < -swipeThresholdPx -> {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showChooser = true
                                    offsetX = 0f
                                }
                                else -> offsetX = 0f
                            }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val task = state.task
                if (task != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                translationX = animatedOffset,
                                rotationZ = animatedOffset / 60f,
                            ),
                    ) {
                        if (state.aheadOfSchedule) {
                            AheadOfScheduleHint()
                            Spacer(Modifier.height(8.dp))
                        }
                        TaskCard(
                            task = task,
                            tier = state.tier,
                            now = now,
                            modifier = Modifier.clickable { onAcceptTask(task.id) },
                        )
                        if (state.steps.isNotEmpty()) {
                            Spacer(Modifier.height(20.dp))
                            StepsProgress(
                                done = state.checkedStepIds.size,
                                total = state.steps.size,
                            )
                            Spacer(Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                items(state.steps, key = { it.id }) { step ->
                                    val checked = step.id in state.checkedStepIds
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.toggleStep(step.id)
                                            }
                                            .padding(vertical = 4.dp),
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.toggleStep(step.id)
                                            },
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            step.content,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                textDecoration = if (checked) TextDecoration.LineThrough else null,
                                            ),
                                            color = if (checked)
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                            else MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (state.emptyState) {
                    EmptyQueueState(
                        onAdd = { showQuickAdd = true },
                        onOpenTasks = onOpenTasks,
                    )
                }
            }

            // Bottom action area: defer + focus mini-row, then big Done CTA
            if (state.task != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showChooser = true }) {
                        Text("← Not now", style = MaterialTheme.typography.titleMedium)
                    }
                    TextButton(onClick = {
                        viewModel.currentTaskId()?.let { onAcceptTask(it) }
                    }) {
                        Text("Focus mode →", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.markCurrentDone()
                    },
                    enabled = state.canMarkDone,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(64.dp),
                ) {
                    Text(
                        if (state.steps.isNotEmpty() && !state.allStepsChecked)
                            "Check every step to finish"
                        else "Mark done ✓",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                Spacer(Modifier.height(96.dp))
            } else {
                Spacer(Modifier.height(96.dp))
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showQuickAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            text = { Text("Add") },
            icon = { Text("+", style = MaterialTheme.typography.titleLarge) },
        )

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp),
        )
    }

    if (showChooser) {
        SwipeChooserSheet(
            onChoose = { kind ->
                viewModel.snoozeCurrent(kind)
                showChooser = false
            },
            onDismiss = { showChooser = false },
        )
    }
    if (showQuickAdd) {
        QuickAddSheet(
            taskRepository = taskRepository,
            onAdded = {
                showQuickAdd = false
                viewModel.onResume()
            },
            onDismiss = { showQuickAdd = false },
        )
    }
}

@Composable
private fun TodayCountChip(count: Int) {
    if (count <= 0) {
        Text(
            "Start the day",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        return
    }
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            "✓ $count today",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun AheadOfScheduleHint() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            "Ahead of schedule — early start",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StepsProgress(done: Int, total: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            "$done of $total steps",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyQueueState(onAdd: () -> Unit, onOpenTasks: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp),
    ) {
        Text(
            "Nothing to decide right now.",
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Brain off. Come back later, or set something up.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onAdd) { Text("Add a task") }
            TextButton(onClick = onOpenTasks) { Text("Browse all") }
        }
    }
}

@Composable
private fun UpdateBanner(onDownload: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE8F1FF),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Update available",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    "Tap to download the latest build",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            TextButton(onClick = onDownload) { Text("Download") }
            TextButton(onClick = onDismiss) { Text("Later") }
        }
    }
}
