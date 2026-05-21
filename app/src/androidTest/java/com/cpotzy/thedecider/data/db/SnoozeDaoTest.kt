package com.cpotzy.thedecider.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cpotzy.thedecider.data.db.entities.SnoozeEntity
import com.cpotzy.thedecider.data.db.entities.SnoozeKind
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
class SnoozeDaoTest {
    private lateinit var db: AppDatabase

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun teardown() = db.close()

    @Test fun activeForReturnsActiveSnooze() = runTest {
        val taskId = db.taskDao().insert(TaskEntity(
            title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        ))
        val now = Instant.parse("2026-01-01T12:00:00Z")
        val until = Instant.parse("2026-01-01T18:00:00Z")
        db.snoozeDao().insert(SnoozeEntity(
            taskId = taskId, until = until, kind = SnoozeKind.LATER_TODAY, createdAt = now,
        ))
        val active = db.snoozeDao().activeFor(taskId, now)
        assertEquals(until, active?.until)
    }

    @Test fun expiredSnoozeNotReturned() = runTest {
        val taskId = db.taskDao().insert(TaskEntity(
            title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        ))
        val now = Instant.parse("2026-01-01T20:00:00Z")
        db.snoozeDao().insert(SnoozeEntity(
            taskId = taskId,
            until = Instant.parse("2026-01-01T18:00:00Z"),
            kind = SnoozeKind.LATER_TODAY,
            createdAt = Instant.parse("2026-01-01T12:00:00Z"),
        ))
        assertNull(db.snoozeDao().activeFor(taskId, now))
    }
}
