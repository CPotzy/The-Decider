package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ContextFilterTest {
    private val filter = ContextFilter()

    private fun task(timeWindow: TimeWindow, energy: Energy = Energy.LOW, duration: Duration = Duration.QUICK) = Task(
        id = 1, title = "X", cadence = Cadence.DAILY, energy = energy, duration = duration,
        timeWindow = timeWindow, isActive = true,
        createdAt = java.time.Instant.parse("2026-01-01T00:00:00Z"), lastDoneAt = null,
    )

    @Test fun `morning window task passes when its morning`() {
        val passes = filter.matches(task(TimeWindow.MORNING), LocalTime.of(8, 0), ModeChip.All)
        assertTrue(passes)
    }

    @Test fun `morning window task fails when its afternoon`() {
        val passes = filter.matches(task(TimeWindow.MORNING), LocalTime.of(14, 0), ModeChip.All)
        assertFalse(passes)
    }

    @Test fun `anytime task passes anytime`() {
        val passes = filter.matches(task(TimeWindow.ANYTIME), LocalTime.of(23, 30), ModeChip.All)
        assertTrue(passes)
    }

    @Test fun `night only allows anytime tasks`() {
        val passes = filter.matches(task(TimeWindow.MORNING), LocalTime.of(3, 0), ModeChip.All)
        assertFalse(passes)
    }

    @Test fun `low energy chip rejects high energy task`() {
        val passes = filter.matches(
            task(TimeWindow.ANYTIME, energy = Energy.HIGH),
            LocalTime.of(10, 0),
            ModeChip.LowEnergy,
        )
        assertFalse(passes)
    }

    @Test fun `quick chip rejects medium duration task`() {
        val passes = filter.matches(
            task(TimeWindow.ANYTIME, duration = Duration.MEDIUM),
            LocalTime.of(10, 0),
            ModeChip.Quick,
        )
        assertFalse(passes)
    }

    @Test fun `ten min chip accepts short and quick`() {
        val short = filter.matches(
            task(TimeWindow.ANYTIME, duration = Duration.SHORT),
            LocalTime.of(10, 0),
            ModeChip.TenMin,
        )
        val quick = filter.matches(
            task(TimeWindow.ANYTIME, duration = Duration.QUICK),
            LocalTime.of(10, 0),
            ModeChip.TenMin,
        )
        assertTrue(short)
        assertTrue(quick)
    }
}
