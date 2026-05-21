package com.cpotzy.thedecider.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PressureTierTest {
    @Test fun `pressure 0 daily is IN_WINDOW`() =
        assertEquals(PressureTier.IN_WINDOW, PressureTier.forPressure(0.0, Cadence.DAILY))

    @Test fun `pressure 1 daily is IN_WINDOW`() =
        assertEquals(PressureTier.IN_WINDOW, PressureTier.forPressure(1.0, Cadence.DAILY))

    @Test fun `pressure 1_1 daily is OVERDUE`() =
        assertEquals(PressureTier.OVERDUE, PressureTier.forPressure(1.1, Cadence.DAILY))

    @Test fun `anytime is ANYTIME tier regardless of pressure`() =
        assertEquals(PressureTier.ANYTIME, PressureTier.forPressure(99.0, Cadence.ANYTIME))

    @Test fun `oneoff is ANYTIME tier regardless of pressure`() =
        assertEquals(PressureTier.ANYTIME, PressureTier.forPressure(0.0, Cadence.ONEOFF))
}
