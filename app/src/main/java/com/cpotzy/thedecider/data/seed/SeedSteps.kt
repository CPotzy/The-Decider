package com.cpotzy.thedecider.data.seed

object SeedSteps {
    val byTitle: Map<String, List<String>> = mapOf(
        // Daily
        "Vacuum downstairs and upstairs" to listOf(
            "Get the vacuum out",
            "Vacuum downstairs floors",
            "Empty the vacuum if full",
            "Vacuum upstairs floors",
            "Put the vacuum away",
        ),
        "Dishes (muci & suci)" to listOf(
            "Scrape food into compost",
            "Soap and sponge muci (impure)",
            "Rinse muci",
            "Soap and sponge suci (pure)",
            "Rinse suci",
            "Stack on drying rack",
        ),
        "Write journal" to listOf(
            "Open the journal",
            "Write today's date",
            "Write one thing you noticed today",
            "Write one thing you're grateful for",
        ),
        "Clean kitchen counter" to listOf(
            "Move items off the counter",
            "Spray the counter with cleaner",
            "Wipe the counter down",
            "Put items back",
        ),
        "Put all dishes away" to listOf(
            "Dishes from drying rack to cupboard",
            "Cutlery to the drawer",
            "Pots to the lower cupboard",
        ),
        "Clean up workspace" to listOf(
            "Stack any loose papers",
            "Put pens and cables away",
            "Wipe the desk surface",
        ),
        "Morning skincare (moisturizer + sunscreen)" to listOf(
            "Apply moisturizer",
            "Apply sunscreen",
        ),
        "Arti/Puja" to listOf(
            "Light incense",
            "Light the lamp",
            "Recite the mantra",
            "Offer flowers",
        ),
        "Pick flowers and offer" to listOf(
            "Get a clean small container",
            "Pick fresh flowers from the garden",
            "Rinse the flowers gently",
            "Place flowers at the altar",
        ),
        "Make offering plate" to listOf(
            "Wash a small plate",
            "Place the selected foods on the plate",
            "Cover the plate with a clean cloth",
        ),
        "Listen to a class" to listOf(
            "Open YouTube or the app",
            "Pick a class",
            "Press play",
        ),

        // Bi-daily
        "Exercise (HIIT workout)" to listOf(
            "Warm-up (2 min light cardio)",
            "Round 1 (3 min)",
            "Round 2 (3 min)",
            "Round 3 (3 min)",
            "Cool-down stretch (2 min)",
        ),
        "Weights" to listOf(
            "Warm-up (light reps)",
            "Upper body set",
            "Lower body set",
            "Cool-down stretch",
        ),
        "Fold and iron laundry" to listOf(
            "Sort the pile by type (shirts, pants, etc.)",
            "Fold everything that doesn't need ironing",
            "Iron formal/wrinkled items",
            "Stack folded clothes by destination drawer",
        ),
        "Take washing out & hang" to listOf(
            "Open the washing machine",
            "Transfer wet clothes into the basket",
            "Hang on the line",
        ),
        "Put washing machine on" to listOf(
            "Load clothes",
            "Add detergent",
            "Select the cycle",
            "Press start",
        ),
        "Take washing in" to listOf(
            "Gather clothes off the line",
            "Fold loosely into the basket",
        ),
        "Put folded clothes away" to listOf(
            "Sort the pile by drawer/cupboard",
            "Place in drawers",
        ),

        // Weekly
        "Clean bathroom" to listOf(
            "Spray toilet cleaner and leave",
            "Wipe sink and mirror",
            "Scrub the toilet bowl",
            "Wipe the floor",
            "Take out bathroom trash",
        ),
        "Change bed sheets and vacuum bed" to listOf(
            "Strip the sheets",
            "Vacuum the mattress",
            "Put the fitted sheet on",
            "Put the duvet and pillows back",
        ),
        "Clean shower" to listOf(
            "Remove products from shelves",
            "Spray cleaner on walls and floor",
            "Scrub the walls and floor",
            "Rinse and squeegee dry",
        ),
        "Send invoices" to listOf(
            "Open the accounting app",
            "Check this week's logged work",
            "Send invoice to each client",
        ),
        "Dust altar" to listOf(
            "Remove items carefully",
            "Dust the altar surface",
            "Replace items mindfully",
        ),
        "Dust bedroom" to listOf(
            "Top of dresser",
            "Nightstands",
            "Headboard",
            "Lampshades",
        ),
        "Face mask" to listOf(
            "Wash your face",
            "Apply the mask",
            "Wait 15 minutes",
            "Rinse off",
        ),
        "Vacuum under bed" to listOf(
            "Pull the bed forward a little",
            "Vacuum the area",
            "Push the bed back",
        ),

        // Biweekly
        "Clean fridge" to listOf(
            "Take everything out",
            "Throw out expired items",
            "Wipe down the shelves",
            "Put items back organized",
            "Wipe the outside",
        ),
        "Change duvet cover" to listOf(
            "Remove old cover",
            "Turn new cover inside out",
            "Grab the corners of the duvet through the cover",
            "Shake the duvet down into place",
        ),

        // Monthly
        "Reorganize pantry section" to listOf(
            "Pick one shelf or section",
            "Empty it",
            "Wipe the shelf",
            "Categorize items",
            "Put them back organized",
        ),
        "Clean car" to listOf(
            "Take out trash and personal items",
            "Vacuum the interior",
            "Wipe the dash and console",
            "Hose down the exterior",
            "Soap the body",
            "Rinse",
        ),
        "Vacuum couch" to listOf(
            "Remove cushions",
            "Vacuum the frame",
            "Vacuum both sides of each cushion",
            "Put cushions back",
        ),

        // Bimonthly
        "Mow the lawn" to listOf(
            "Clear the lawn of obstacles",
            "Check the mower has fuel/charge",
            "Mow in straight lines",
            "Edge the borders",
        ),
        "Clean windows" to listOf(
            "Spray cleaner inside",
            "Wipe inside in S-pattern",
            "Spray cleaner outside",
            "Wipe outside in S-pattern",
        ),
        "Clean AC filter" to listOf(
            "Turn off the AC",
            "Remove the filter",
            "Rinse under water",
            "Let it dry, then reinstall",
        ),

        // Anytime
        "Empty trash bin" to listOf(
            "Tie the bag",
            "Take it to the outside bin",
        ),
        "Compost" to listOf(
            "Empty the indoor compost bin",
            "Rinse the bin",
        ),
        "Recycling" to listOf(
            "Bring the bag to the recycling area",
            "Separate by type (paper, plastic, glass)",
            "Place each in the correct bin",
        ),
    )

    fun forTitle(title: String): List<String> = byTitle[title] ?: emptyList()
}
