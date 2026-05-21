package com.cpotzy.thedecider.data.repo

import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.TaskDao
import com.cpotzy.thedecider.data.db.entities.CompletionType
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.domain.model.Task

class TaskRepository(
    private val taskDao: TaskDao,
    private val completionDao: CompletionDao,
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
}
