package com.cpotzy.thedecider.data.repo

import com.cpotzy.thedecider.data.db.dao.SnoozeDao
import com.cpotzy.thedecider.data.db.entities.SnoozeEntity
import com.cpotzy.thedecider.data.db.entities.SnoozeKind
import com.cpotzy.thedecider.domain.time.Clock
import java.time.Duration as JDuration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class SnoozeRepository(
    private val snoozeDao: SnoozeDao,
    private val clock: Clock,
) {
    suspend fun snoozeLaterToday(taskId: Long) {
        val now = clock.now()
        val until = now.plus(JDuration.ofHours(3))
        snoozeDao.insert(SnoozeEntity(
            taskId = taskId, until = until, kind = SnoozeKind.LATER_TODAY, createdAt = now,
        ))
    }

    suspend fun snoozeTomorrow(taskId: Long, zone: ZoneId = ZoneId.systemDefault()) {
        val now = clock.now()
        val tomorrowStart = LocalDate.now(zone).plusDays(1).atTime(LocalTime.of(5, 0)).atZone(zone).toInstant()
        snoozeDao.insert(SnoozeEntity(
            taskId = taskId, until = tomorrowStart, kind = SnoozeKind.TOMORROW, createdAt = now,
        ))
    }

    suspend fun activeTaskIds(now: Instant): Set<Long> = snoozeDao.activeTaskIds(now).toSet()
}
