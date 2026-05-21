package com.cpotzy.thedecider.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

enum class SnoozeKind { LATER_TODAY, TOMORROW, SKIP_CYCLE }

@Entity(
    tableName = "snoozes",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("taskId"), Index("until")],
)
data class SnoozeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val until: Instant,
    val kind: SnoozeKind,
    val createdAt: Instant,
)
