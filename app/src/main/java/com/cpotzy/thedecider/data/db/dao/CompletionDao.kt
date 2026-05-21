package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.CompletionType
import java.time.Instant

@Dao
interface CompletionDao {
    @Insert suspend fun insert(completion: CompletionEntity): Long

    @Query("""
        SELECT MAX(completedAt) FROM completions
        WHERE taskId = :taskId AND type = :type
    """)
    suspend fun lastOfType(taskId: Long, type: CompletionType = CompletionType.DONE): Instant?

    @Query("SELECT * FROM completions ORDER BY completedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 100): List<CompletionEntity>
}
