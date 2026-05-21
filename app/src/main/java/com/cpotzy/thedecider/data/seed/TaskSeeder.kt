package com.cpotzy.thedecider.data.seed

import android.content.Context
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.data.repo.TaskRepository
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import com.cpotzy.thedecider.domain.time.Clock
import java.io.BufferedReader

data class SeedTask(val title: String, val cadence: Cadence)

object TaskSeeder {
    private val sectionToCadence = mapOf(
        "Daily" to Cadence.DAILY,
        "Bi-daily" to Cadence.BIDAILY,
        "Weekly" to Cadence.WEEKLY,
        "Biweekly" to Cadence.BIWEEKLY,
        "Monthly" to Cadence.MONTHLY,
        "Bimonthly" to Cadence.BIMONTHLY,
        "Anytime" to Cadence.ANYTIME,
    )

    fun parseMarkdown(text: String): List<SeedTask> {
        val tasks = mutableListOf<SeedTask>()
        var current: Cadence? = null
        for (rawLine in text.lineSequence()) {
            val line = rawLine.trim()
            if (line.startsWith("## ")) {
                val header = line.removePrefix("## ").substringBefore(" (").trim()
                current = sectionToCadence[header]
            } else if (line.startsWith("- ") && current != null) {
                val title = line.removePrefix("- ").trim()
                if (title.isNotEmpty()) tasks.add(SeedTask(title, current))
            }
        }
        return tasks
    }

    suspend fun seedIfEmpty(context: Context, repo: TaskRepository, clock: Clock) {
        if (repo.count() > 0) return
        val text = context.assets.open("tasks-list.md").bufferedReader().use(BufferedReader::readText)
        val seedTasks = parseMarkdown(text)
        val now = clock.now()
        val entities = seedTasks.map { seed ->
            TaskEntity(
                title = seed.title,
                cadence = seed.cadence,
                energy = defaultEnergy(seed.title),
                duration = defaultDuration(seed.title),
                timeWindow = defaultTimeWindow(seed.title),
                createdAt = now,
            )
        }
        repo.insertAll(entities)
    }

    private fun defaultEnergy(title: String): Energy = when {
        title.contains("HIIT", ignoreCase = true) || title.contains("Weights", ignoreCase = true) -> Energy.HIGH
        title.contains("Mow", ignoreCase = true) || title.contains("Clean", ignoreCase = true) -> Energy.MEDIUM
        else -> Energy.LOW
    }

    private fun defaultDuration(title: String): Duration = when {
        title.contains("HIIT", ignoreCase = true) || title.contains("Clean bathroom", ignoreCase = true) -> Duration.MEDIUM
        title.contains("Brush", ignoreCase = true) || title.contains("Floss", ignoreCase = true) ||
            title.contains("Scrape", ignoreCase = true) || title.contains("skincare", ignoreCase = true) -> Duration.QUICK
        else -> Duration.SHORT
    }

    private fun defaultTimeWindow(title: String): TimeWindow = when {
        title.contains("morning", ignoreCase = true) || title.contains("Morning", ignoreCase = false) -> TimeWindow.MORNING
        title.contains("night", ignoreCase = true) -> TimeWindow.EVENING
        title.contains("HIIT", ignoreCase = true) -> TimeWindow.MORNING
        else -> TimeWindow.ANYTIME
    }
}
