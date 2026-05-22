package com.cpotzy.thedecider.domain.select

import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy

data class ModeChip(
    val label: String,
    val energyFilter: Energy? = null,
    val maxDuration: Duration? = null,
    val companyComing: Boolean = false,
) {
    companion object {
        val All = ModeChip(label = "All")
        val LowEnergy = ModeChip(label = "Low energy", energyFilter = Energy.LOW)
        val TenMin = ModeChip(label = "10 min", maxDuration = Duration.SHORT)
        val Quick = ModeChip(label = "Quick", maxDuration = Duration.QUICK)
        val CompanyComing = ModeChip(label = "Company", companyComing = true)

        val defaults = listOf(All, LowEnergy, TenMin, Quick, CompanyComing)
    }
}
