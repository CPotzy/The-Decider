package com.cpotzy.thedecider.data.seed

object SeedDependencies {
    val byTitle: Map<String, List<String>> = mapOf(
        "Mop the floor" to listOf("Vacuum downstairs and upstairs"),
    )

    fun forTitle(title: String): List<String> = byTitle[title] ?: emptyList()
}
