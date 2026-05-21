package com.cpotzy.thedecider.data.seed

/**
 * Canonical step definitions for each task.
 *
 * Each step has a [content] string and an optional [durationSeconds].
 * Durations are estimates used by the in-app timer in focus mode.
 * They are deliberately a little tight — the point of the timer is to
 * provide gentle pressure, not a perfect estimate.
 *
 * Edit these freely. On the next launch after install, [TaskSeeder]
 * backfills any missing steps and updates durations on existing rows.
 */
data class StepDef(val content: String, val durationSeconds: Int? = null)

object SeedSteps {
    val byTitle: Map<String, List<StepDef>> = mapOf(
        // Daily
        "Vacuum downstairs and upstairs" to listOf(
            StepDef("Clear the floor (buckets, chairs, rubbish bin, coffee table)", 180),
            StepDef("Get the vacuum out", 30),
            StepDef("Vacuum downstairs floors", 240),
            StepDef("Vacuum the stairs", 180),
            StepDef("Empty the vacuum if full", 60),
            StepDef("Vacuum upstairs floors", 240),
            StepDef("Put the vacuum away", 30),
        ),
        "Dishes (muci & suci)" to listOf(
            StepDef("Collect all dirty dishes from around the house", 180),
            StepDef("Scrape food into compost", 60),
            StepDef("Soap and sponge muci (impure)", 180),
            StepDef("Rinse muci", 60),
            StepDef("Soap and sponge suci (pure)", 180),
            StepDef("Rinse suci", 60),
            StepDef("Stack on drying rack", 60),
        ),
        "Write journal" to listOf(
            StepDef("Open the journal", 15),
            StepDef("Write today's date", 15),
            StepDef("Write one thing you noticed today", 120),
            StepDef("Write one thing you're grateful for", 120),
        ),
        "Clean kitchen counter" to listOf(
            StepDef("Move items off the counter", 60),
            StepDef("Spray the counter with cleaner", 30),
            StepDef("Wipe the counter down", 90),
            StepDef("Put items back", 60),
        ),
        "Put all dishes away" to listOf(
            StepDef("Dishes from drying rack to cupboard", 90),
            StepDef("Cutlery to the drawer", 30),
            StepDef("Pots to the lower cupboard", 60),
        ),
        "Clean up workspace" to listOf(
            StepDef("Stack any loose papers", 60),
            StepDef("Put pens and cables away", 60),
            StepDef("Wipe the desk surface", 60),
        ),
        "Morning skincare (moisturizer + sunscreen)" to listOf(
            StepDef("Apply moisturizer", 30),
            StepDef("Apply sunscreen", 30),
        ),
        "Arti/Puja" to listOf(
            StepDef("Light incense", 30),
            StepDef("Light the lamp", 30),
            StepDef("Recite the mantra", 120),
            StepDef("Offer flowers", 30),
        ),
        "Pick flowers and offer" to listOf(
            StepDef("Get a clean small container", 30),
            StepDef("Pick fresh flowers from the garden", 180),
            StepDef("Rinse the flowers gently", 60),
            StepDef("Place flowers at the altar", 30),
        ),
        "Make offering plate" to listOf(
            StepDef("Wash a small plate", 60),
            StepDef("Place the selected foods on the plate", 90),
            StepDef("Cover the plate with a clean cloth", 15),
        ),
        "Listen to a class" to listOf(
            StepDef("Open YouTube or the app", 30),
            StepDef("Pick a class", 60),
            StepDef("Press play", 5),
        ),

        // Bi-daily
        "Exercise (HIIT workout)" to listOf(
            StepDef("Warm-up (light cardio)", 120),
            StepDef("Round 1", 180),
            StepDef("Round 2", 180),
            StepDef("Round 3", 180),
            StepDef("Cool-down stretch", 120),
        ),
        "Weights" to listOf(
            StepDef("Warm-up (light reps)", 180),
            StepDef("Upper body set", 600),
            StepDef("Lower body set", 600),
            StepDef("Cool-down stretch", 180),
        ),
        "Fold and iron laundry" to listOf(
            StepDef("Sort the pile by type", 120),
            StepDef("Fold everything that doesn't need ironing", 480),
            StepDef("Iron formal/wrinkled items", 480),
            StepDef("Stack folded clothes by destination drawer", 90),
        ),
        "Take washing out & hang" to listOf(
            StepDef("Open the washing machine", 10),
            StepDef("Transfer wet clothes into the basket", 60),
            StepDef("Hang on the line", 300),
        ),
        "Put washing machine on" to listOf(
            StepDef("Load clothes", 60),
            StepDef("Add detergent", 20),
            StepDef("Select the cycle", 15),
            StepDef("Press start", 5),
        ),
        "Take washing in" to listOf(
            StepDef("Gather clothes off the line", 240),
            StepDef("Fold loosely into the basket", 60),
        ),
        "Put folded clothes away" to listOf(
            StepDef("Sort the pile by drawer/cupboard", 90),
            StepDef("Place in drawers", 180),
        ),

        // Weekly
        "Clean bathroom" to listOf(
            StepDef("Spray toilet cleaner and leave", 30),
            StepDef("Wipe sink and mirror", 180),
            StepDef("Scrub the toilet bowl", 120),
            StepDef("Wipe the floor", 180),
            StepDef("Take out bathroom trash", 60),
        ),
        "Change bed sheets and vacuum bed" to listOf(
            StepDef("Strip the sheets", 120),
            StepDef("Vacuum the mattress", 180),
            StepDef("Put the fitted sheet on", 120),
            StepDef("Put the duvet and pillows back", 180),
        ),
        "Clean shower" to listOf(
            StepDef("Remove products from shelves", 60),
            StepDef("Spray cleaner on walls and floor", 60),
            StepDef("Scrub the walls and floor", 300),
            StepDef("Rinse and squeegee dry", 120),
        ),
        "Send invoices" to listOf(
            StepDef("Open the accounting app", 30),
            StepDef("Check this week's logged work", 180),
            StepDef("Send invoice to each client", 300),
        ),
        "Dust altar" to listOf(
            StepDef("Remove items carefully", 60),
            StepDef("Dust the altar surface", 120),
            StepDef("Replace items mindfully", 120),
        ),
        "Dust bedroom" to listOf(
            StepDef("Top of dresser", 120),
            StepDef("Nightstands", 60),
            StepDef("Headboard", 60),
            StepDef("Lampshades", 60),
        ),
        "Face mask" to listOf(
            StepDef("Wash your face", 60),
            StepDef("Apply the mask", 60),
            StepDef("Wait 15 minutes", 900),
            StepDef("Rinse off", 60),
        ),
        "Vacuum under bed" to listOf(
            StepDef("Pull the bed forward a little", 60),
            StepDef("Vacuum the area", 180),
            StepDef("Push the bed back", 60),
        ),
        "Mop the floor" to listOf(
            StepDef("Fill the bucket with warm water + mop solution", 120),
            StepDef("Mop the downstairs floor", 360),
            StepDef("Rinse the mop", 60),
            StepDef("Mop the upstairs floor", 240),
            StepDef("Empty the bucket and put it away", 60),
        ),

        // Biweekly
        "Clean fridge" to listOf(
            StepDef("Take everything out", 300),
            StepDef("Throw out expired items", 180),
            StepDef("Wipe down the shelves", 300),
            StepDef("Put items back organized", 240),
            StepDef("Wipe the outside", 120),
        ),
        "Change duvet cover" to listOf(
            StepDef("Remove old cover", 60),
            StepDef("Turn new cover inside out", 30),
            StepDef("Grab the corners of the duvet through the cover", 60),
            StepDef("Shake the duvet down into place", 60),
        ),

        // Monthly
        "Reorganize pantry section" to listOf(
            StepDef("Pick one shelf or section", 30),
            StepDef("Empty it", 180),
            StepDef("Wipe the shelf", 120),
            StepDef("Categorize items", 180),
            StepDef("Put them back organized", 240),
        ),
        "Clean car" to listOf(
            StepDef("Take out trash and personal items", 180),
            StepDef("Vacuum the interior", 420),
            StepDef("Wipe the dash and console", 180),
            StepDef("Hose down the exterior", 180),
            StepDef("Soap the body", 300),
            StepDef("Rinse", 180),
        ),
        "Vacuum couch" to listOf(
            StepDef("Remove cushions", 30),
            StepDef("Vacuum the frame", 180),
            StepDef("Vacuum both sides of each cushion", 300),
            StepDef("Put cushions back", 30),
        ),

        // Bimonthly
        "Mow the lawn" to listOf(
            StepDef("Clear the lawn of obstacles", 120),
            StepDef("Check the mower has fuel/charge", 60),
            StepDef("Mow in straight lines", 1200),
            StepDef("Edge the borders", 300),
        ),
        "Clean windows" to listOf(
            StepDef("Spray cleaner inside", 60),
            StepDef("Wipe inside in S-pattern", 300),
            StepDef("Spray cleaner outside", 60),
            StepDef("Wipe outside in S-pattern", 300),
        ),
        "Clean AC filter" to listOf(
            StepDef("Turn off the AC", 15),
            StepDef("Remove the filter", 60),
            StepDef("Rinse under water", 120),
            StepDef("Let it dry, then reinstall", 60),
        ),

        // Anytime
        "Empty trash bin" to listOf(
            StepDef("Tie the bag", 15),
            StepDef("Take it to the outside bin", 60),
        ),
        "Compost" to listOf(
            StepDef("Empty the indoor compost bin", 60),
            StepDef("Rinse the bin", 60),
        ),
        "Recycling" to listOf(
            StepDef("Bring the bag to the recycling area", 60),
            StepDef("Separate by type (paper, plastic, glass)", 120),
            StepDef("Place each in the correct bin", 60),
        ),
    )

    fun forTitle(title: String): List<StepDef> = byTitle[title] ?: emptyList()
}
