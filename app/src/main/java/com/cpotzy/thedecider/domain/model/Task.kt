package com.cpotzy.thedecider.domain.model

import java.time.Instant

data class Task(
    val id: Long,
    val title: String,
    val cadence: Cadence,
    val energy: Energy,
    val duration: Duration,
    val timeWindow: TimeWindow,
    val isActive: Boolean,
    val createdAt: Instant,
    val lastDoneAt: Instant?,
)
