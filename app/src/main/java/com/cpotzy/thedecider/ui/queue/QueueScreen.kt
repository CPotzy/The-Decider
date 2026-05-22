package com.cpotzy.thedecider.ui.queue

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
                    onDragStopped = {
                        when {
                            offsetX > swipeThresholdPx -> {
                                viewModel.currentTaskId()?.let { onAcceptTask(it) }
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
                    TaskCard(task = task, tier = state.tier, now = now)
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
                Text("start →", style = MaterialTheme.typography.titleLarge)
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
