package com.cpotzy.thedecider.data.seed

/**
 * Tasks that count as "make the place presentable" — surface-level work the
 * user might want to blast through when guests are about to walk in.
 *
 * In "Company" mode the queue surfaces only these, ignoring cadence and snooze.
 *
 * Add titles here as the seed list grows. Anything not listed defaults to
 * `quickTidy = false`.
 */
object SeedQuickTidy {
    val titles: Set<String> = setOf(
        // Daily surface resets
        "Clean kitchen counter",
        "Put all dishes away",
        "Clean up workspace",
        "Dishes (muci & suci)",
        // Doom-pile collects (the basket sweep is exactly a "make it presentable" pass)
        "Clear lounge into the doom pile",
        "Clear bedroom into the doom pile",
        // Weekly visible-surfaces
        "Clean bathroom",
    )

    fun isQuickTidy(title: String): Boolean = title in titles
}
