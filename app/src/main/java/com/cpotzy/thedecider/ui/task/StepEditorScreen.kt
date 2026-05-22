package com.cpotzy.thedecider.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.data.db.entities.StepEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepEditorScreen(
    viewModel: StepEditorViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    var durationFor by remember { mutableStateOf<StepEntity?>(null) }

    LaunchedEffect(state.lastDeleted) {
        val deleted = state.lastDeleted ?: return@LaunchedEffect
        val result = snackbarHost.showSnackbar(
            message = "Step deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        } else {
            viewModel.clearDeleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Edit steps", style = MaterialTheme.typography.titleLarge)
                        if (state.taskTitle.isNotBlank()) {
                            Text(
                                state.taskTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.addStep() },
                text = { Text("Add step") },
                icon = { Text("+", style = MaterialTheme.typography.titleLarge) },
            )
        },
    ) { padding ->
        if (state.steps.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "No steps yet.",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap + Add step to start a checklist.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(state.steps, key = { _, step -> step.id }) { index, step ->
                    StepRow(
                        step = step,
                        index = index,
                        isFirst = index == 0,
                        isLast = index == state.steps.size - 1,
                        onContentChange = { viewModel.updateContent(step, it) },
                        onChangeDuration = { durationFor = step },
                        onDelete = { viewModel.deleteStep(step) },
                        onMoveUp = { viewModel.moveUp(index) },
                        onMoveDown = { viewModel.moveDown(index) },
                    )
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }

    durationFor?.let { step ->
        DurationPickerSheet(
            current = step.durationSeconds,
            onPick = { seconds ->
                viewModel.updateDuration(step, seconds)
                durationFor = null
            },
            onDismiss = { durationFor = null },
        )
    }
}

@Composable
private fun StepRow(
    step: StepEntity,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onContentChange: (String) -> Unit,
    onChangeDuration: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    var content by remember(step.id, step.content) { mutableStateOf(step.content) }
    val focusRequester = remember { FocusRequester() }

    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isFirst,
                        modifier = Modifier.size(28.dp),
                    ) { Text("▲", style = MaterialTheme.typography.labelSmall) }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLast,
                        modifier = Modifier.size(28.dp),
                    ) { Text("▼", style = MaterialTheme.typography.labelSmall) }
                }
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = {
                            content = it
                            onContentChange(it)
                        },
                        placeholder = { Text("Step ${index + 1}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        minLines = 1,
                        maxLines = 4,
                    )
                }
                IconButton(onClick = onDelete) {
                    Text("✕", style = MaterialTheme.typography.titleMedium)
                }
            }
            Row(
                modifier = Modifier.padding(start = 36.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onChangeDuration, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        step.durationSeconds?.let { "⏱ ${formatSeconds(it)}" } ?: "⏱ no target",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationPickerSheet(
    current: Int?,
    onPick: (Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    val options: List<Pair<Int?, String>> = listOf(
        null to "No target",
        30 to "30 sec",
        60 to "1 min",
        120 to "2 min",
        180 to "3 min",
        300 to "5 min",
        600 to "10 min",
        900 to "15 min",
        1800 to "30 min",
    )
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text("Step duration", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            options.forEach { (secs, label) ->
                val selected = secs == current
                Surface(
                    onClick = { onPick(secs) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun formatSeconds(seconds: Int): String = when {
    seconds < 60 -> "${seconds}s"
    seconds < 3600 -> {
        val m = seconds / 60
        val s = seconds % 60
        if (s == 0) "${m} min" else "${m}m ${s}s"
    }
    else -> "${seconds / 3600}h ${seconds % 3600 / 60}m"
}
