package com.cpotzy.thedecider.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

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
            BottomAppBar {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { viewModel.finishTask() },
                    modifier = Modifier.padding(end = 16.dp),
                ) {
                    Text("Done with task")
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
        Text(
            "Just do it.",
            style = MaterialTheme.typography.headlineLarge,
        )
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        state.steps.forEach { step ->
            val checked = step.id in state.checkedStepIds
            Surface(
                onClick = { onToggle(step.id) },
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
        Spacer(Modifier.height(96.dp)) // room above the bottom bar
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
        Spacer(Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                current.content,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(32.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onMarkDone,
            enabled = !checked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (checked) "Step done ✓" else "Done with step")
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                onClick = onPrev,
                enabled = state.currentFocusIndex > 0,
            ) { Text("← Previous") }
            TextButton(
                onClick = onNext,
                enabled = state.currentFocusIndex < totalSteps - 1,
            ) { Text("Next →") }
        }
    }
}
