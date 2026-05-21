package com.cpotzy.thedecider.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpotzy.thedecider.data.repo.CompletionRepository
import com.cpotzy.thedecider.data.repo.SnoozeRepository
import com.cpotzy.thedecider.data.repo.TaskRepository
import com.cpotzy.thedecider.data.update.UpdateChecker
import com.cpotzy.thedecider.data.update.UpdateInfo
import com.cpotzy.thedecider.domain.model.PressureTier
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.select.ModeChip
import com.cpotzy.thedecider.domain.select.PressureCalculator
import com.cpotzy.thedecider.domain.select.SelectionService
import com.cpotzy.thedecider.domain.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId

data class QueueUiState(
    val task: Task? = null,
    val pressure: Double = 0.0,
    val tier: PressureTier = PressureTier.IN_WINDOW,
    val mode: ModeChip = ModeChip.All,
    val modeChips: List<ModeChip> = ModeChip.defaults,
    val emptyState: Boolean = false,
    val update: UpdateInfo? = null,
    val updateDismissed: Boolean = false,
)

class QueueViewModel(
    private val taskRepository: TaskRepository,
    private val completionRepository: CompletionRepository,
    private val snoozeRepository: SnoozeRepository,
    private val selectionService: SelectionService,
    private val pressureCalc: PressureCalculator,
    private val updateChecker: UpdateChecker,
    private val clock: Clock,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val _state = MutableStateFlow(QueueUiState())
    val state: StateFlow<QueueUiState> = _state.asStateFlow()

    init {
        refresh()
        checkForUpdate()
    }

    fun onResume() = refresh()

    fun dismissUpdateBanner() {
        _state.value = _state.value.copy(updateDismissed = true)
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            val info = updateChecker.checkForUpdate()
            if (info != null) _state.value = _state.value.copy(update = info)
        }
    }

    fun setMode(mode: ModeChip) {
        _state.value = _state.value.copy(mode = mode)
        refresh()
    }

    fun currentTaskId(): Long? = _state.value.task?.id

    fun snoozeCurrent(kind: SnoozeKindChoice) {
        val current = _state.value.task ?: return
        viewModelScope.launch {
            when (kind) {
                SnoozeKindChoice.LATER_TODAY -> snoozeRepository.snoozeLaterToday(current.id)
                SnoozeKindChoice.TOMORROW -> snoozeRepository.snoozeTomorrow(current.id, zone)
                SnoozeKindChoice.SKIP_CYCLE -> completionRepository.markSkippedKeepPressure(current.id)
            }
            refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val now = clock.now()
            val candidates = taskRepository.listEligibleForSelection(now)
            val snoozed = snoozeRepository.activeTaskIds(now)
            val picked = selectionService.pickNext(
                candidates = candidates,
                snoozedIds = snoozed,
                now = now,
                zone = zone,
                mode = _state.value.mode,
            )
            if (picked == null) {
                _state.value = _state.value.copy(task = null, emptyState = true)
            } else {
                val pressure = pressureCalc.pressure(picked, now)
                val tier = PressureTier.forPressure(pressure, picked.cadence)
                _state.value = _state.value.copy(task = picked, pressure = pressure, tier = tier, emptyState = false)
            }
        }
    }
}

enum class SnoozeKindChoice { LATER_TODAY, TOMORROW, SKIP_CYCLE }
