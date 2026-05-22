package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.PressureTier
import com.cpotzy.thedecider.domain.model.Task
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

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
        random: Random = Random.Default,
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
        val tierOrder = listOf(PressureTier.OVERDUE, PressureTier.IN_WINDOW, PressureTier.ANYTIME)
        for (tier in tierOrder) {
            val bucket = scored.filter { it.tier == tier }
            if (bucket.isNotEmpty()) {
                return weightedPick(bucket, random)
            }
        }
        return null
    }

    private fun weightedPick(bucket: List<Scored>, random: Random): Task {
        val weights = bucket.map { scored ->
            val cadenceDays = scored.task.cadence.cadenceDays
            if (cadenceDays == null) {
                scored.pressure + 1.0
            } else {
                val daysLate = scored.pressure * cadenceDays
                kotlin.math.sqrt(daysLate) + 1.0
            }
        }
        val totalWeight = weights.sum()
        val roll = random.nextDouble() * totalWeight
        var acc = 0.0
        weights.forEachIndexed { i, w ->
            acc += w
            if (roll <= acc) return bucket[i].task
        }
        return bucket.last().task
    }
}
