package com.cpotzy.thedecider.ui.quickadd

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cpotzy.thedecider.data.repo.TaskRepository
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration as DurationTag
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import kotlinx.coroutines.launch

private data class QuickAddForm(
    val title: String = "",
    val cadence: Cadence = Cadence.ONEOFF,
    val energy: Energy = Energy.MEDIUM,
    val duration: DurationTag = DurationTag.SHORT,
    val timeWindow: TimeWindow = TimeWindow.ANYTIME,
)

private val cadenceOptions = listOf(
    Cadence.ONEOFF to "One-off",
    Cadence.DAILY to "Daily",
    Cadence.BIDAILY to "Bi-daily",
    Cadence.WEEKLY to "Weekly",
    Cadence.BIWEEKLY to "Biweekly",
    Cadence.MONTHLY to "Monthly",
    Cadence.BIMONTHLY to "Bimonthly",
    Cadence.ANYTIME to "Anytime",
)
private val energyOptions = listOf(Energy.LOW to "Low", Energy.MEDIUM to "Med", Energy.HIGH to "High")
private val durationOptions = listOf(
    DurationTag.QUICK to "Quick",
    DurationTag.SHORT to "Short",
    DurationTag.MEDIUM to "Medium",
    DurationTag.LONG to "Long",
)
private val windowOptions = listOf(
    TimeWindow.ANYTIME to "Anytime",
    TimeWindow.MORNING to "Morning",
    TimeWindow.AFTERNOON to "Afternoon",
    TimeWindow.EVENING to "Evening",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    taskRepository: TaskRepository,
    onAdded: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var form by remember { mutableStateOf(QuickAddForm()) }
    val canSubmit = form.title.isNotBlank()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Quick add", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = form.title,
                onValueChange = { form = form.copy(title = it) },
                placeholder = { Text("Task title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            ChipSection("Cadence", cadenceOptions, form.cadence) {
                form = form.copy(cadence = it)
            }
            ChipSection("Energy", energyOptions, form.energy) {
                form = form.copy(energy = it)
            }
            ChipSection("Duration", durationOptions, form.duration) {
                form = form.copy(duration = it)
            }
            ChipSection("Time window", windowOptions, form.timeWindow) {
                form = form.copy(timeWindow = it)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = canSubmit,
                    onClick = {
                        val snapshot = form
                        scope.launch {
                            taskRepository.createCustomTask(
                                title = snapshot.title.trim(),
                                cadence = snapshot.cadence,
                                energy = snapshot.energy,
                                duration = snapshot.duration,
                                timeWindow = snapshot.timeWindow,
                            )
                            onAdded()
                        }
                    },
                ) { Text("Add") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipSection(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, text) ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    label = { Text(text) },
                )
            }
        }
    }
}
