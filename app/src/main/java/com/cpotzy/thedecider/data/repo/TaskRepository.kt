package com.cpotzy.thedecider.data.repo

import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.TaskDao
import com.cpotzy.thedecider.data.db.entities.CompletionType
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import com.cpotzy.thedecider.domain.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

class TaskRepository(
    private val taskDao: TaskDao,
    private val completionDao: CompletionDao,
    private val clock: Clock,
) {
    suspend fun insertAll(entities: List<TaskEntity>) = taskDao.insertAll(entities)
    suspend fun count(): Int = taskDao.count()

    suspend fun listActiveWithLastDone(): List<Task> {
        return taskDao.listActive().map { e ->
            val lastDone = completionDao.lastOfType(e.id, CompletionType.DONE)
            Task(
                id = e.id,
                title = e.title,
                cadence = e.cadence,
                energy = e.energy,
                duration = e.duration,
                timeWindow = e.timeWindow,
                isActive = e.isActive,
                createdAt = e.createdAt,
                lastDoneAt = lastDone,
            )
        }
    }

    suspend fun setActive(id: Long, isActive: Boolean) = taskDao.setActive(id, isActive)

    suspend fun listAllRaw(): List<TaskEntity> = taskDao.listAll()

    suspend fun createCustomTask(
        title: String,
        cadence: Cadence,
        energy: Energy,
        duration: Duration,
        timeWindow: TimeWindow,
    ): Long {
        val entity = TaskEntity(
            title = title,
            cadence = cadence,
            energy = energy,
            duration = duration,
            timeWindow = timeWindow,
            createdAt = clock.now(),
            isUserCreated = true,
        )
        return taskDao.insert(entity)
    }

    suspend fun listEligibleForSelection(now: Instant = clock.now()): List<Task> {
        return listActiveWithLastDone().filter { task ->
            val cadenceDays = task.cadence.cadenceDays
            if (cadenceDays == null) return@filter true
            val ref = task.lastDoneAt ?: task.createdAt
            ChronoUnit.HOURS.between(ref, now) >= cadenceDays * 24
        }
    }
}
