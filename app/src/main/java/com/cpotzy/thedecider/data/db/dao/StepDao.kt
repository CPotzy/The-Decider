package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cpotzy.thedecider.data.db.entities.StepEntity

@Dao
interface StepDao {
    @Insert suspend fun insertAll(steps: List<StepEntity>)

    @Query("SELECT * FROM steps WHERE taskId = :taskId ORDER BY `order` ASC")
    suspend fun forTask(taskId: Long): List<StepEntity>

    @Query("UPDATE steps SET durationSeconds = :durationSeconds WHERE id = :id")
    suspend fun updateDuration(id: Long, durationSeconds: Int?)

    @Query("DELETE FROM steps WHERE taskId = :taskId")
    suspend fun deleteByTask(taskId: Long)
}
