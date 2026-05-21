package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cpotzy.thedecider.data.db.entities.SnoozeEntity
import java.time.Instant

@Dao
interface SnoozeDao {
    @Insert suspend fun insert(snooze: SnoozeEntity): Long

    @Query("""
        SELECT * FROM snoozes
        WHERE taskId = :taskId AND until > :now
        ORDER BY until DESC LIMIT 1
    """)
    suspend fun activeFor(taskId: Long, now: Instant): SnoozeEntity?

    @Query("""
        SELECT DISTINCT taskId FROM snoozes WHERE until > :now
    """)
    suspend fun activeTaskIds(now: Instant): List<Long>
}
