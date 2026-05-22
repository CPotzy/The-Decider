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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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

    LaunchedEffect(Unit) { viewModel.onResume() }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 32.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onOpenTasks) { Text("Tasks") }
            TextButton(onClick = onOpenHistory) { Text("History") }
            TextButton(onClick = onOpenSettings) { Text("Settings") }
        }
        val update = state.update
        if (update != null && !state.updateDismissed) {
            UpdateBanner(
                onDownload = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.releaseUrl))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    viewModel.dismissUpdateBanner()
                },
                onDismiss = { viewModel.dismissUpdateBanner() },
            )
            Spacer(Modifier.height(16.dp))
        }
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
                                onAcceptTask(currentId)
                                offsetX = 0f
                            }
                            offsetX < -swipeThresholdPx -> {
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
                        Text(
                            "Ahead of schedule — early start",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    TaskCard(
                        task = task,
                        tier = state.tier,
                        now = now,
                        modifier = Modifier.clickable { onAcceptTask(task.id) },
                    )
                    if (state.steps.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(state.steps, key = { it.id }) { step ->
                                val checked = step.id in state.checkedStepIds
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleStep(step.id) }
                                        .padding(vertical = 4.dp),
                                ) {
                                    Checkbox(checked = checked, onCheckedChange = { viewModel.toggleStep(step.id) })
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        step.content,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            textDecoration = if (checked) TextDecoration.LineThrough else null,
                                        ),
                                        color = if (checked)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (state.emptyState) {
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
                        "Come back later, or try a different filter above.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { if (state.task != null) showChooser = true },
                enabled = state.task != null,
            ) {
                Text("← later", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(
                onClick = { viewModel.currentTaskId()?.let { onAcceptTask(it) } },
                enabled = state.task != null,
            ) {
                Text("Focus", style = MaterialTheme.typography.titleMedium)
            }
            Button(
                onClick = { viewModel.markCurrentDone() },
                enabled = state.canMarkDone,
            ) {
                Text("Done", style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
        FloatingActionButton(
            onClick = { showQuickAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp),
        ) {
            Text("+", style = MaterialTheme.typography.headlineLarge)
        }
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
