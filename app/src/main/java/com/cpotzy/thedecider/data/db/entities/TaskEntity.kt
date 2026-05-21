package com.cpotzy.thedecider.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import java.time.Instant

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val cadence: Cadence,
    val energy: Energy,
    val duration: Duration,
    val timeWindow: TimeWindow,
    val isActive: Boolean = true,
    val createdAt: Instant,
)
