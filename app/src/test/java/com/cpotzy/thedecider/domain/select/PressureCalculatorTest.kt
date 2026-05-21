package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class PressureCalculatorTest {
    private val now = Instant.parse("2026-05-21T12:00:00Z")
    private val calc = PressureCalculator()

    private fun task(cadence: Cadence, lastDoneAt: Instant?) = Task(
        id = 1, title = "X", cadence = cadence, energy = Energy.LOW, duration = Duration.QUICK,
        timeWindow = TimeWindow.ANYTIME, isActive = true,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"), lastDoneAt = lastDoneAt,
    )

    @Test fun `daily done today has pressure 0`() {
        val t = task(Cadence.DAILY, now)
        assertEquals(0.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `daily done 1 day ago has pressure 0`() {
        val t = task(Cadence.DAILY, now.minus(1, ChronoUnit.DAYS))
        assertEquals(0.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `daily done 2 days ago has pressure 1`() {
        val t = task(Cadence.DAILY, now.minus(2, ChronoUnit.DAYS))
        assertEquals(1.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `weekly done 14 days ago has pressure 1`() {
        val t = task(Cadence.WEEKLY, now.minus(14, ChronoUnit.DAYS))
        assertEquals(1.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `anytime returns small constant`() {
        val t = task(Cadence.ANYTIME, null)
        assertEquals(0.05, calc.pressure(t, now), 0.001)
    }

    @Test fun `never done daily with createdAt 5 days ago has pressure 4`() {
        val t = Task(
            id = 1, title = "X", cadence = Cadence.DAILY, energy = Energy.LOW, duration = Duration.QUICK,
            timeWindow = TimeWindow.ANYTIME, isActive = true,
            createdAt = now.minus(5, ChronoUnit.DAYS), lastDoneAt = null,
        )
        assertEquals(4.0, calc.pressure(t, now), 0.001)
    }

    @Test fun `oneoff returns small constant`() {
        val t = task(Cadence.ONEOFF, null)
        assertEquals(0.05, calc.pressure(t, now), 0.001)
    }
}
