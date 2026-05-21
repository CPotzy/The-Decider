package com.cpotzy.thedecider.domain.model

import org.junit.Test
import org.junit.Assert.assertEquals
import java.time.LocalTime

class TimeWindowTest {
    @Test fun `06_00 is MORNING`() = assertEquals(TimeWindow.MORNING, TimeWindow.atLocalTime(LocalTime.of(6, 0)))
    @Test fun `11_59 is MORNING`() = assertEquals(TimeWindow.MORNING, TimeWindow.atLocalTime(LocalTime.of(11, 59)))
    @Test fun `12_00 is AFTERNOON`() = assertEquals(TimeWindow.AFTERNOON, TimeWindow.atLocalTime(LocalTime.of(12, 0)))
    @Test fun `16_59 is AFTERNOON`() = assertEquals(TimeWindow.AFTERNOON, TimeWindow.atLocalTime(LocalTime.of(16, 59)))
    @Test fun `17_00 is EVENING`() = assertEquals(TimeWindow.EVENING, TimeWindow.atLocalTime(LocalTime.of(17, 0)))
    @Test fun `22_59 is EVENING`() = assertEquals(TimeWindow.EVENING, TimeWindow.atLocalTime(LocalTime.of(22, 59)))
    @Test fun `23_00 is NIGHT`() = assertEquals(TimeWindow.NIGHT, TimeWindow.atLocalTime(LocalTime.of(23, 0)))
    @Test fun `04_59 is NIGHT`() = assertEquals(TimeWindow.NIGHT, TimeWindow.atLocalTime(LocalTime.of(4, 59)))
    @Test fun `05_00 is MORNING`() = assertEquals(TimeWindow.MORNING, TimeWindow.atLocalTime(LocalTime.of(5, 0)))
}
