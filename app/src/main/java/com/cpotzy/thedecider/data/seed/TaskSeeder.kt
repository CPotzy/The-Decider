package com.cpotzy.thedecider.data.seed

import android.content.Context
import com.cpotzy.thedecider.data.db.dao.StepDao
import com.cpotzy.thedecider.data.db.entities.StepEntity
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

    suspend fun seedIfEmpty(context: Context, repo: TaskRepository, stepDao: StepDao, clock: Clock) {
        val text = context.assets.open("tasks-list.md").bufferedReader().use(BufferedReader::readText)
        val seedTasks = parseMarkdown(text)
        val now = clock.now()

        if (repo.count() == 0) {
            val entities = seedTasks.map { seed ->
                TaskEntity(
                    title = seed.title,
                    cadence = seed.cadence,
                    energy = defaultEnergy(seed.title),
                    duration = defaultDuration(seed.title),
                    timeWindow = defaultTimeWindow(seed.title),
                    createdAt = now,
                    dependsOnTitles = SeedDependencies.forTitle(seed.title),
                )
            }
            repo.insertAll(entities)
        } else {
            reconcileTasks(repo, seedTasks, now)
        }
        reconcileSteps(repo, stepDao)
    }

    private suspend fun reconcileTasks(repo: TaskRepository, seedTasks: List<SeedTask>, now: java.time.Instant) {
        val seedByTitle = seedTasks.associateBy { it.title }
        val allInDb = repo.listAllRaw()
        val dbByTitle = allInDb.associateBy { it.title }

        // Add tasks that exist in markdown but not in DB
        val toInsert = seedTasks
            .filter { it.title !in dbByTitle.keys }
            .map { seed ->
                TaskEntity(
                    title = seed.title,
                    cadence = seed.cadence,
                    energy = defaultEnergy(seed.title),
                    duration = defaultDuration(seed.title),
                    timeWindow = defaultTimeWindow(seed.title),
                    createdAt = now,
                    dependsOnTitles = SeedDependencies.forTitle(seed.title),
                )
            }
        if (toInsert.isNotEmpty()) repo.insertAll(toInsert)

        // Update dependency lists on existing seed-managed tasks if they have drifted
        allInDb.forEach { row ->
            if (row.isUserCreated) return@forEach
            val expected = SeedDependencies.forTitle(row.title)
            if (row.dependsOnTitles != expected) {
                repo.updateDependsOn(row.id, expected)
            }
        }

        // Reactivate tasks that exist in markdown and are currently inactive
        allInDb.forEach { row ->
            if (row.isUserCreated) return@forEach
            if (row.title in seedByTitle.keys && !row.isActive) {
                repo.setActive(row.id, true)
            }
        }

        // Deactivate seed-managed tasks that are no longer in markdown
        // (user-created tasks live outside this reconciliation loop)
        allInDb.forEach { row ->
            if (row.isUserCreated) return@forEach
            if (row.title !in seedByTitle.keys && row.isActive) {
                repo.setActive(row.id, false)
            }
        }
    }

    private suspend fun reconcileSteps(repo: TaskRepository, stepDao: StepDao) {
        val tasks = repo.listActiveWithLastDone()
        tasks.forEach { task ->
            val seed = SeedSteps.forTitle(task.title)
            if (seed.isEmpty()) return@forEach
            val existing = stepDao.forTask(task.id)
            val matches = existing.size == seed.size && existing.zip(seed).all { (row, def) ->
                row.content == def.content &&
                    row.durationSeconds == def.durationSeconds
            }
            if (matches) return@forEach
            stepDao.deleteByTask(task.id)
            val rows = seed.mapIndexed { i, def ->
                StepEntity(
                    taskId = task.id,
                    order = i,
                    content = def.content,
                    durationSeconds = def.durationSeconds,
                )
            }
            stepDao.insertAll(rows)
        }
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
