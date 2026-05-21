package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import java.time.LocalTime

class ContextFilter {
    fun matches(task: Task, currentTime: LocalTime, mode: ModeChip): Boolean {
        if (!matchesTimeWindow(task, currentTime)) return false
        if (mode.energyFilter != null && task.energy != mode.energyFilter) return false
        if (mode.maxDuration != null && task.duration.maxMinutes > mode.maxDuration.maxMinutes) return false
        return true
    }

    private fun matchesTimeWindow(task: Task, currentTime: LocalTime): Boolean {
        if (task.timeWindow == TimeWindow.ANYTIME) return true
        val window = TimeWindow.atLocalTime(currentTime)
        if (window == TimeWindow.NIGHT) return false
        return window == task.timeWindow
    }
}
