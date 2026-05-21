package com.cpotzy.thedecider.domain.model

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class CadenceTest {
    @Test fun `daily cadenceDays is 1`() = assertEquals(1L, Cadence.DAILY.cadenceDays)
    @Test fun `bidaily cadenceDays is 2`() = assertEquals(2L, Cadence.BIDAILY.cadenceDays)
    @Test fun `weekly cadenceDays is 7`() = assertEquals(7L, Cadence.WEEKLY.cadenceDays)
    @Test fun `biweekly cadenceDays is 14`() = assertEquals(14L, Cadence.BIWEEKLY.cadenceDays)
    @Test fun `monthly cadenceDays is 30`() = assertEquals(30L, Cadence.MONTHLY.cadenceDays)
    @Test fun `bimonthly cadenceDays is 60`() = assertEquals(60L, Cadence.BIMONTHLY.cadenceDays)
    @Test fun `anytime cadenceDays is null`() = assertNull(Cadence.ANYTIME.cadenceDays)
    @Test fun `oneoff cadenceDays is null`() = assertNull(Cadence.ONEOFF.cadenceDays)
}
