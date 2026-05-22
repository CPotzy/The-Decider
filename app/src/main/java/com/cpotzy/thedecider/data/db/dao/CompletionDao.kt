package com.cpotzy.thedecider.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.CompletionType
import java.time.Instant

data class CompletionWithTitle(
    val id: Long,
    val taskId: Long,
    val taskTitle: String,
    val completedAt: Instant,
    val type: CompletionType,
)

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

    @Query("""
        SELECT c.id AS id, c.taskId AS taskId, t.title AS taskTitle,
               c.completedAt AS completedAt, c.type AS type
        FROM completions c
        INNER JOIN tasks t ON c.taskId = t.id
        ORDER BY c.completedAt DESC
        LIMIT :limit
    """)
    suspend fun recentWithTitle(limit: Int = 200): List<CompletionWithTitle>

    @Query("DELETE FROM completions")
    suspend fun deleteAll()
}
