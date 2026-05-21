package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.Task
import com.cpotzy.thedecider.domain.model.TimeWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class SelectionServiceTest {
    private val now = Instant.parse("2026-05-21T10:00:00Z")
    private val zone = ZoneId.of("UTC")
    private val service = SelectionService(PressureCalculator(), ContextFilter())

    private fun task(
        id: Long, cadence: Cadence, lastDoneAt: Instant?,
        timeWindow: TimeWindow = TimeWindow.ANYTIME,
        energy: Energy = Energy.LOW,
        duration: Duration = Duration.QUICK,
    ) = Task(
        id = id, title = "T$id", cadence = cadence, energy = energy, duration = duration,
        timeWindow = timeWindow, isActive = true,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"), lastDoneAt = lastDoneAt,
    )

    @Test fun `returns null when no candidates`() {
        val picked = service.pickNext(
            candidates = emptyList(),
            snoozedIds = emptySet(),
            now = now,
            zone = zone,
            mode = ModeChip.All,
            random = Random(0),
        )
        assertNull(picked)
    }

    @Test fun `excludes snoozed tasks`() {
        val candidates = listOf(task(1, Cadence.DAILY, now.minus(3, ChronoUnit.DAYS)))
        val picked = service.pickNext(candidates, setOf(1L), now, zone, ModeChip.All, Random(0))
        assertNull(picked)
    }

    @Test fun `prefers overdue tier over in_window`() {
        val candidates = listOf(
            task(1, Cadence.DAILY, now.minus(1, ChronoUnit.HOURS)),
            task(2, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS)),
        )
        val picked = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(0))
        assertEquals(2L, picked?.id)
    }

    @Test fun `falls back to anytime tier when nothing else`() {
        val candidates = listOf(task(1, Cadence.ANYTIME, null))
        val picked = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(0))
        assertNotNull(picked)
        assertEquals(1L, picked?.id)
    }

    @Test fun `low energy chip narrows candidates`() {
        val candidates = listOf(
            task(1, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS), energy = Energy.HIGH),
            task(2, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS), energy = Energy.LOW),
        )
        val picked = service.pickNext(candidates, emptySet(), now, zone, ModeChip.LowEnergy, Random(0))
        assertEquals(2L, picked?.id)
    }

    @Test fun `weighted random within tier is deterministic given seed`() {
        val candidates = listOf(
            task(1, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS)),
            task(2, Cadence.DAILY, now.minus(5, ChronoUnit.DAYS)),
        )
        val first = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(42))
        val second = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(42))
        assertEquals(first?.id, second?.id)
    }

    @Test fun `daily done today is excluded from candidates by caller`() {
        val candidates = listOf(task(1, Cadence.DAILY, now))
        val picked = service.pickNext(candidates, emptySet(), now, zone, ModeChip.All, Random(0))
        assertEquals(1L, picked?.id)
    }
}
