package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Task
import java.time.Duration
import java.time.Instant

class PressureCalculator {
    fun pressure(task: Task, now: Instant): Double {
        val cadenceDays = task.cadence.cadenceDays ?: return ANYTIME_PRESSURE
        val reference = task.lastDoneAt ?: task.createdAt
        val daysSince = Duration.between(reference, now).toHours().toDouble() / 24.0
        val raw = (daysSince - cadenceDays) / cadenceDays
        return if (raw < 0) 0.0 else raw
    }

    companion object {
        const val ANYTIME_PRESSURE = 0.05
    }
}
