package com.cpotzy.thedecider.data.repo

import com.cpotzy.thedecider.data.db.dao.StepDao
import com.cpotzy.thedecider.data.db.dao.TaskDao
import com.cpotzy.thedecider.data.db.entities.StepEntity
import com.cpotzy.thedecider.data.db.entities.TaskEntity

class StepRepository(
    private val stepDao: StepDao,
    private val taskDao: TaskDao,
) {
    suspend fun stepsFor(taskId: Long): List<StepEntity> = stepDao.forTask(taskId)
    suspend fun task(taskId: Long): TaskEntity? = taskDao.byId(taskId)

    suspend fun addStep(taskId: Long, content: String, durationSeconds: Int?): Long {
        val existing = stepDao.forTask(taskId)
        val nextOrder = (existing.maxOfOrNull { it.order } ?: -1) + 1
        val id = stepDao.insert(
            StepEntity(
                taskId = taskId,
                order = nextOrder,
                content = content,
                durationSeconds = durationSeconds,
            ),
        )
        markEdited(taskId)
        return id
    }

    suspend fun updateStep(step: StepEntity) {
        stepDao.update(step)
        markEdited(step.taskId)
    }

    suspend fun deleteStep(step: StepEntity) {
        stepDao.delete(step.id)
        markEdited(step.taskId)
    }

    suspend fun reorder(taskId: Long, idsInNewOrder: List<Long>) {
        stepDao.reorder(taskId, idsInNewOrder)
        markEdited(taskId)
    }

    private suspend fun markEdited(taskId: Long) {
        taskDao.setStepsEdited(taskId, true)
    }
}
