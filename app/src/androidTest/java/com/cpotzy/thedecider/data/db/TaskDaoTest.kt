package com.cpotzy.thedecider.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cpotzy.thedecider.data.db.entities.TaskEntity
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var db: AppDatabase
    private val dao get() = db.taskDao()

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun teardown() = db.close()

    @Test fun insertAndRetrieve() = runTest {
        val id = dao.insert(sampleTask("Test task"))
        val retrieved = dao.byId(id)
        assertEquals("Test task", retrieved?.title)
    }

    @Test fun listActiveExcludesInactive() = runTest {
        dao.insert(sampleTask("Active"))
        val inactiveId = dao.insert(sampleTask("Inactive"))
        dao.setActive(inactiveId, false)
        val active = dao.listActive()
        assertEquals(1, active.size)
        assertEquals("Active", active[0].title)
    }

    private fun sampleTask(title: String) = TaskEntity(
        title = title,
        cadence = Cadence.DAILY,
        energy = Energy.LOW,
        duration = Duration.QUICK,
        timeWindow = TimeWindow.ANYTIME,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )
}
