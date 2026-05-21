package com.cpotzy.thedecider.data.seed

import com.cpotzy.thedecider.domain.model.Cadence
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class TaskSeederTest {
    private val sampleMarkdown = """
        # The-Decider — Task List

        ## Daily
        - Vacuum downstairs and upstairs
        - Brush teeth night

        ## Weekly
        - Clean bathroom
        - Dust altar

        ## Anytime (as needed)
        - Empty trash bin
    """.trimIndent()

    @Test fun parsesCadenceSections() {
        val parsed = TaskSeeder.parseMarkdown(sampleMarkdown)
        val byCadence = parsed.groupBy { it.cadence }
        assertEquals(2, byCadence[Cadence.DAILY]?.size)
        assertEquals(2, byCadence[Cadence.WEEKLY]?.size)
        assertEquals(1, byCadence[Cadence.ANYTIME]?.size)
    }

    @Test fun extractsTitles() {
        val parsed = TaskSeeder.parseMarkdown(sampleMarkdown)
        val titles = parsed.map { it.title }
        assertTrue(titles.contains("Vacuum downstairs and upstairs"))
        assertTrue(titles.contains("Clean bathroom"))
        assertTrue(titles.contains("Empty trash bin"))
    }
}
