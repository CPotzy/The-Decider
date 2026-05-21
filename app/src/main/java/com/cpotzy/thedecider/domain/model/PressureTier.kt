package com.cpotzy.thedecider.domain.model

enum class PressureTier { OVERDUE, IN_WINDOW, ANYTIME;

    companion object {
        fun forPressure(pressure: Double, cadence: Cadence): PressureTier = when {
            cadence == Cadence.ANYTIME || cadence == Cadence.ONEOFF -> ANYTIME
            pressure > 1.0 -> OVERDUE
            else -> IN_WINDOW
        }
    }
}
