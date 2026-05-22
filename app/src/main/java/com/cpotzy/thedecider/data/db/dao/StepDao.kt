package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cpotzy.thedecider.data.db.entities.StepEntity

@Dao
interface StepDao {
    @Insert suspend fun insertAll(steps: List<StepEntity>)
    @Insert suspend fun insert(step: StepEntity): Long
    @Update suspend fun update(step: StepEntity)

    @Query("SELECT * FROM steps WHERE taskId = :taskId ORDER BY `order` ASC")
    suspend fun forTask(taskId: Long): List<StepEntity>

    @Query("UPDATE steps SET durationSeconds = :durationSeconds WHERE id = :id")
    suspend fun updateDuration(id: Long, durationSeconds: Int?)

    @Query("UPDATE steps SET `order` = :order WHERE id = :id")
    suspend fun setOrder(id: Long, order: Int)

    @Transaction
    suspend fun reorder(taskId: Long, idsInNewOrder: List<Long>) {
        idsInNewOrder.forEachIndexed { i, id -> setOrder(id, i) }
    }

    @Query("DELETE FROM steps WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM steps WHERE taskId = :taskId")
    suspend fun deleteByTask(taskId: Long)
}
