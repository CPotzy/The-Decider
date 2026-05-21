package com.cpotzy.thedecider.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.CompletionType
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class CompletionDaoTest {
    private lateinit var db: AppDatabase

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun teardown() = db.close()

    @Test fun lastDoneIsLatestDoneCompletion() = runTest {
        val taskId = db.taskDao().insert(TaskEntity(
            title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        ))
        val t1 = Instant.parse("2026-01-02T10:00:00Z")
        val t2 = Instant.parse("2026-01-03T10:00:00Z")
        db.completionDao().insert(CompletionEntity(taskId = taskId, completedAt = t1, type = CompletionType.DONE))
        db.completionDao().insert(CompletionEntity(taskId = taskId, completedAt = t2, type = CompletionType.DONE))
        assertEquals(t2, db.completionDao().lastOfType(taskId, CompletionType.DONE))
    }

    @Test fun skippedDoesNotCountAsDone() = runTest {
        val taskId = db.taskDao().insert(TaskEntity(
            title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        ))
        db.completionDao().insert(CompletionEntity(
            taskId = taskId, completedAt = Instant.parse("2026-01-02T10:00:00Z"),
            type = CompletionType.SKIPPED_PRESSURE_KEPT,
        ))
        assertNull(db.completionDao().lastOfType(taskId, CompletionType.DONE))
    }
}
