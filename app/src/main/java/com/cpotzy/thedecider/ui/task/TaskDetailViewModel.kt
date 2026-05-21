package com.cpotzy.thedecider.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpotzy.thedecider.data.db.entities.StepEntity
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.data.repo.CompletionRepository
import com.cpotzy.thedecider.data.repo.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val task: TaskEntity? = null,
    val steps: List<StepEntity> = emptyList(),
    val checkedStepIds: Set<Long> = emptySet(),
    val focusMode: Boolean = false,
    val currentFocusIndex: Int = 0,
    val finished: Boolean = false,
)

class TaskDetailViewModel(
    private val taskId: Long,
    private val stepRepository: StepRepository,
    private val completionRepository: CompletionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskDetailUiState())
    val state: StateFlow<TaskDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val task = stepRepository.task(taskId)
            val steps = stepRepository.stepsFor(taskId)
            _state.value = _state.value.copy(task = task, steps = steps)
        }
    }

    fun toggleStep(stepId: Long) {
        val checked = _state.value.checkedStepIds
        _state.value = _state.value.copy(
            checkedStepIds = if (stepId in checked) checked - stepId else checked + stepId,
        )
    }

    fun toggleFocusMode() {
        _state.value = _state.value.copy(focusMode = !_state.value.focusMode)
    }

    fun nextFocusStep() {
        val s = _state.value
        if (s.currentFocusIndex < s.steps.size - 1) {
            _state.value = s.copy(currentFocusIndex = s.currentFocusIndex + 1)
        }
    }

    fun prevFocusStep() {
        val s = _state.value
        if (s.currentFocusIndex > 0) {
            _state.value = s.copy(currentFocusIndex = s.currentFocusIndex - 1)
        }
    }

    fun markCurrentFocusDone() {
        val s = _state.value
        val current = s.steps.getOrNull(s.currentFocusIndex) ?: return
        val newChecked = s.checkedStepIds + current.id
        val nextIndex = if (s.currentFocusIndex < s.steps.size - 1) s.currentFocusIndex + 1 else s.currentFocusIndex
        _state.value = s.copy(checkedStepIds = newChecked, currentFocusIndex = nextIndex)
    }

    fun finishTask() {
        viewModelScope.launch {
            completionRepository.markDone(taskId)
            _state.value = _state.value.copy(finished = true)
        }
    }
}
