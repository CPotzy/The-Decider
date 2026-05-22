package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.PressureTier
import com.cpotzy.thedecider.domain.model.Task
import java.time.Instant
import java.time.ZoneId

class SelectionService(
    private val pressureCalc: PressureCalculator,
    private val contextFilter: ContextFilter,
) {
    data class Scored(val task: Task, val pressure: Double, val tier: PressureTier)

    fun pickNext(
        candidates: List<Task>,
        snoozedIds: Set<Long>,
        now: Instant,
        zone: ZoneId,
        mode: ModeChip,
    ): Task? {
        val currentLocalTime = now.atZone(zone).toLocalTime()
        val scored = candidates
            .asSequence()
            .filter { it.id !in snoozedIds }
            .filter { contextFilter.matches(it, currentLocalTime, mode) }
            .map { task ->
                val p = pressureCalc.pressure(task, now)
                Scored(task, p, PressureTier.forPressure(p, task.cadence))
            }
            .toList()
        if (scored.isEmpty()) return null
        return orderedHead(scored)?.task
    }

    private fun orderedHead(scored: List<Scored>): Scored? {
        // Deterministic ordering: tier (OVERDUE > IN_WINDOW > ANYTIME),
        // then pressure desc, then task id asc for a stable tiebreak.
        // Same plan until something is done/skipped/snoozed.
        val tierRank = mapOf(
            PressureTier.OVERDUE to 0,
            PressureTier.IN_WINDOW to 1,
            PressureTier.ANYTIME to 2,
        )
        return scored.minWithOrNull(
            compareBy<Scored> { tierRank[it.tier] ?: 99 }
                .thenByDescending { it.pressure }
                .thenBy { it.task.id }
        )
    }
}
