package com.cpotzy.thedecider.ui.queue

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.ui.queue.components.ModeChipRow
import com.cpotzy.thedecider.ui.queue.components.SwipeChooserSheet
import com.cpotzy.thedecider.ui.queue.components.TaskCard
import java.time.Instant

@Composable
fun QueueScreen(
    viewModel: QueueViewModel,
    now: Instant = Instant.now(),
) {
    val state by viewModel.state.collectAsState()
    var offsetX by remember { mutableStateOf(0f) }
    var showChooser by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 120.dp.toPx() }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "swipeOffset")

    val dragState = rememberDraggableState { delta -> offsetX += delta }

    Column(modifier = Modifier.fillMaxSize().padding(top = 32.dp)) {
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
                                viewModel.acceptCurrent()
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
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            translationX = animatedOffset,
                            rotationZ = animatedOffset / 60f,
                        ),
                ) {
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
                onClick = { viewModel.acceptCurrent() },
                enabled = state.task != null,
            ) {
                Text("done →", style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(16.dp))
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
}
