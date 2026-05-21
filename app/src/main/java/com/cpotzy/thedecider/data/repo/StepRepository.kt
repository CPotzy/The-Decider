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
}
