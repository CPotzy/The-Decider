package com.cpotzy.thedecider.ui.manage

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
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.data.repo.TaskRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    taskRepository: TaskRepository,
    onBack: () -> Unit,
) {
    var tasks by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        tasks = taskRepository.listAllRaw().sortedWith(
            compareByDescending<TaskEntity> { it.isActive }
                .thenBy { it.cadence.ordinal }
                .thenBy { it.title.lowercase() },
        )
    }

    LaunchedEffect(Unit) { reload() }

    var pendingDelete by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onToggleActive = { newActive ->
                            scope.launch {
                                taskRepository.setActive(task.id, newActive)
                                reload()
                            }
                        },
                        onDelete = { pendingDelete = task },
                    )
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this task?") },
            text = { Text("\"${target.title}\" and all its history will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        taskRepository.delete(target.id)
                        pendingDelete = null
                        reload()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun TaskRow(
    task: TaskEntity,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (task.isActive) 2.dp else 0.dp,
        color = if (task.isActive)
            MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (task.isActive) FontWeight.Medium else FontWeight.Normal,
                    ),
                    color = if (task.isActive)
                        MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        task.cadence.name.lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    if (task.isUserCreated) {
                        Text(
                            "· custom",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Switch(checked = task.isActive, onCheckedChange = onToggleActive)
            if (task.isUserCreated) {
                IconButton(onClick = onDelete) {
                    Text("✕", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
