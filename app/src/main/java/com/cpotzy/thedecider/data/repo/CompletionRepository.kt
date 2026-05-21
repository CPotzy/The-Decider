package com.cpotzy.thedecider.data.repo

import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.TaskDao
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.CompletionType
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.time.Clock

class CompletionRepository(
    private val completionDao: CompletionDao,
    private val taskDao: TaskDao,
    private val clock: Clock,
) {
    suspend fun markDone(taskId: Long) {
        val now = clock.now()
        completionDao.insert(CompletionEntity(taskId = taskId, completedAt = now, type = CompletionType.DONE))
        val task = taskDao.byId(taskId) ?: return
        if (task.cadence == Cadence.ONEOFF) {
            taskDao.setActive(taskId, false)
        }
    }

    suspend fun markSkippedKeepPressure(taskId: Long) {
        completionDao.insert(CompletionEntity(
            taskId = taskId, completedAt = clock.now(), type = CompletionType.SKIPPED_PRESSURE_KEPT,
        ))
    }
}
