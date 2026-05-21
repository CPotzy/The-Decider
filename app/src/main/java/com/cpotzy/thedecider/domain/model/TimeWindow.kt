package com.cpotzy.thedecider.domain.model

import java.time.LocalTime

enum class TimeWindow {
    MORNING, AFTERNOON, EVENING, NIGHT, ANYTIME;

    companion object {
        fun atLocalTime(t: LocalTime): TimeWindow = when {
            t >= LocalTime.of(5, 0) && t < LocalTime.of(12, 0) -> MORNING
            t >= LocalTime.of(12, 0) && t < LocalTime.of(17, 0) -> AFTERNOON
            t >= LocalTime.of(17, 0) && t < LocalTime.of(23, 0) -> EVENING
            else -> NIGHT
        }
    }
}
