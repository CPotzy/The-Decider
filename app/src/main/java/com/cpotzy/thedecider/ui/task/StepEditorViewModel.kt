package com.cpotzy.thedecider.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpotzy.thedecider.data.db.entities.StepEntity
import com.cpotzy.thedecider.data.repo.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StepEditorUiState(
    val taskTitle: String = "",
    val steps: List<StepEntity> = emptyList(),
    val lastDeleted: StepEntity? = null,
)

class StepEditorViewModel(
    private val taskId: Long,
    private val stepRepository: StepRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StepEditorUiState())
    val state: StateFlow<StepEditorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val task = stepRepository.task(taskId)
            val steps = stepRepository.stepsFor(taskId)
            _state.value = _state.value.copy(
                taskTitle = task?.title.orEmpty(),
                steps = steps,
            )
        }
    }

    fun addStep() {
        viewModelScope.launch {
            stepRepository.addStep(taskId, content = "", durationSeconds = null)
            reload()
        }
    }

    fun updateContent(step: StepEntity, content: String) {
        viewModelScope.launch {
            stepRepository.updateStep(step.copy(content = content))
            reload()
        }
    }

    fun updateDuration(step: StepEntity, durationSeconds: Int?) {
        viewModelScope.launch {
            stepRepository.updateStep(step.copy(durationSeconds = durationSeconds))
            reload()
        }
    }

    fun deleteStep(step: StepEntity) {
        viewModelScope.launch {
            stepRepository.deleteStep(step)
            _state.value = _state.value.copy(lastDeleted = step)
            reload()
        }
    }

    fun undoDelete() {
        val deleted = _state.value.lastDeleted ?: return
        viewModelScope.launch {
            stepRepository.addStep(
                taskId = deleted.taskId,
                content = deleted.content,
                durationSeconds = deleted.durationSeconds,
            )
            _state.value = _state.value.copy(lastDeleted = null)
            reload()
        }
    }

    fun clearDeleted() {
        _state.value = _state.value.copy(lastDeleted = null)
    }

    fun moveUp(index: Int) {
        if (index <= 0) return
        val steps = _state.value.steps.toMutableList()
        val item = steps.removeAt(index)
        steps.add(index - 1, item)
        persistOrder(steps)
    }

    fun moveDown(index: Int) {
        val steps = _state.value.steps.toMutableList()
        if (index >= steps.size - 1) return
        val item = steps.removeAt(index)
        steps.add(index + 1, item)
        persistOrder(steps)
    }

    private fun persistOrder(newOrder: List<StepEntity>) {
        _state.value = _state.value.copy(steps = newOrder)
        viewModelScope.launch {
            stepRepository.reorder(taskId, newOrder.map { it.id })
            reload()
        }
    }

    private suspend fun reload() {
        _state.value = _state.value.copy(steps = stepRepository.stepsFor(taskId))
    }
}
