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
            StepDef("Vacuum downstairs floors", 270),
            StepDef("Vacuum upstairs floors", 240),
            StepDef("Put the vacuum away and plug in to charge", 60),
        ),
        "Wash muci dishes" to listOf(
            StepDef("Collect muci dishes from around the house", 180),
            StepDef("Wash and stack on the drying rack", 480),
        ),
        "Wash suci dishes" to listOf(
            StepDef("Collect suci dishes from around the house", 180),
            StepDef("Wash and stack on the drying rack", 480),
        ),
        "Write journal" to listOf(
            StepDef("Open the journal and write today's date", 30),
            StepDef("Write one thing you noticed today", 120),
            StepDef("Write one thing you're grateful for", 120),
        ),
        "Clean kitchen counter" to listOf(
            StepDef("Move items off the counter", 60),
            StepDef("Spray and wipe", 120),
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

        // Bi-daily
        "Fold and iron laundry" to listOf(
            StepDef("Sort the pile by type", 180),
            StepDef("Fold everything that doesn't need ironing", 720),
            StepDef("Iron formal/wrinkled items", 720),
            StepDef("Stack folded clothes by destination drawer", 135),
        ),
        "Take washing out & hang" to listOf(
            StepDef("Empty the machine into the basket", 70),
            StepDef("Hang on the line", 300),
        ),
        "Put washing machine on" to listOf(
            StepDef("Separate whites from colors", 60),
            StepDef("Load, add detergent, pick cycle, start", 90),
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
            StepDef("Spray toilet cleaner and leave it to work", 30),
            StepDef("Wipe sink and mirror", 180),
            StepDef("Scrub the toilet bowl", 120),
            StepDef("Wipe the floor", 180),
            StepDef("Empty bathroom bin and put in a fresh liner", 90),
        ),
        "Change bed sheets and vacuum bed" to listOf(
            StepDef("Strip the sheets", 120),
            StepDef("Vacuum the mattress", 180),
            StepDef("Put the fitted sheet on", 120),
            StepDef("Put the duvet and pillows back", 180),
        ),
        "Clean shower" to listOf(
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
            StepDef("Desk", 120),
            StepDef("Vinyl records", 120),
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
        "Clear lounge into the doom pile" to listOf(
            StepDef("Grab the doom-pile basket from its spot"),
            StepDef("5-min sweep: anything not where it lives goes in the basket", 300),
            StepDef("Park the basket somewhere out of the walkway"),
        ),
        "Clear bedroom into the doom pile" to listOf(
            StepDef("Grab the doom-pile basket from its spot"),
            StepDef("5-min sweep: anything not where it lives goes in the basket", 300),
            StepDef("Park the basket somewhere out of the walkway"),
        ),
        "Sort the doom pile" to listOf(
            StepDef("Bring the basket somewhere comfy — couch, table, floor"),
            StepDef("5-min sort. For each thing: where does it LIVE? Put it there, or set on a 'donate / toss' pile", 300),
            StepDef("5-min sort again — keep going while you've got the momentum", 300),
            StepDef("Toss / donate / shelve the leftover piles"),
            StepDef("Return the basket to its spot"),
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
            StepDef("Remove old cover", 360),
            StepDef("Turn new cover inside out", 180),
            StepDef("Grab the corners of the duvet through the cover", 360),
            StepDef("Shake the duvet down into place", 300),
        ),

        // Monthly — one task per kitchen section (instead of pick-random).
        // All follow the same template; edit individually as needed.
        "Reorganize kitchen drawers" to reorganizeSteps(),
        "Reorganize pantry" to reorganizeSteps(),
        "Reorganize above oven" to reorganizeSteps(),
        "Reorganize below oven" to reorganizeSteps(),
        "Reorganize below sink" to reorganizeSteps(),
        "Reorganize stove cupboard" to reorganizeSteps(),
        "Reorganize island cupboard" to reorganizeSteps(),
        "Reorganize buckets area" to reorganizeSteps(),
        "Vacuum couch" to listOf(
            StepDef("Remove cushions", 30),
            StepDef("Vacuum the frame", 180),
            StepDef("Vacuum behind the couch", 120),
            StepDef("Vacuum both sides of each cushion", 300),
            StepDef("Put cushions back", 30),
        ),

        // Bimonthly
        "Clean windows (inside)" to listOf(
            StepDef("Spray and wipe inside in S-pattern, pane by pane", 720),
        ),
        "Clean windows (outside)" to listOf(
            StepDef("Spray and wipe outside in S-pattern, pane by pane", 720),
        ),
    )

    private fun reorganizeSteps(): List<StepDef> = listOf(
        StepDef("Empty it", 360),
        StepDef("Wipe the surface", 240),
        StepDef("Categorize items", 360),
        StepDef("Put them back organized", 480),
    )

    fun forTitle(title: String): List<StepDef> = byTitle[title] ?: emptyList()
}
