package ai.algo1.marketbasket.core.domain.today

enum class SmartPickColor { Red, Green, Blue }
enum class SmartPickAction { ViewRecipe, AddToList }

data class SmartPickCard(
    val id: String,
    val imageKey: String,
    val title: String,
    val description: String,
    val tag: String,
    val tagColor: SmartPickColor,
    val cta: String,
    val action: SmartPickAction,
    val progress: Progress? = null,
    val topBadge: String? = null,
    val discountBadge: String? = null,
    val comparison: Comparison? = null,
) {
    data class Progress(val color: SmartPickColor, val percent: Int, val label: String)
    data class Comparison(val fromLabel: String, val fromPrice: String, val toLabel: String, val toPrice: String)
}

/** Port of SMART_PICK_CARDS (smartPicksData.ts). imageKey = image basename (note swap-kraft-cabot -> kraft-cabot-cheddar). */
object SmartPicks {
    val all: List<SmartPickCard> = listOf(
        SmartPickCard(
            id = "creamy-garlic-pasta", imageKey = "creamy-garlic-pasta",
            title = "Creamy Garlic Pasta", description = "Saved from TikTok.",
            tag = "From your ideas", tagColor = SmartPickColor.Red,
            cta = "View recipe", action = SmartPickAction.ViewRecipe,
            progress = SmartPickCard.Progress(SmartPickColor.Green, 71, "5 of 7 ingredients"),
        ),
        SmartPickCard(
            id = "swap-kraft-cabot", imageKey = "kraft-cabot-cheddar",
            title = "Swap Kraft for Cabot", description = "Same quantity, lower price.",
            tag = "Make it cheaper", tagColor = SmartPickColor.Green,
            cta = "Add to list", action = SmartPickAction.AddToList,
            topBadge = "Save ${'$'}2.19",
            comparison = SmartPickCard.Comparison("Kraft", "${'$'}5.49", "Cabot", "${'$'}3.30"),
        ),
        SmartPickCard(
            id = "fresh-strawberries", imageKey = "fresh-strawberries",
            title = "Fresh Strawberries", description = "16 oz punnet, on sale this week.",
            tag = "Weekly deal", tagColor = SmartPickColor.Red,
            cta = "View recipe", action = SmartPickAction.ViewRecipe,
            discountBadge = "2 for ${'$'}5",
            progress = SmartPickCard.Progress(SmartPickColor.Red, 60, "Save ${'$'}1.98"),
        ),
        SmartPickCard(
            id = "chicken-broccoli-rice", imageKey = "chicken-broccoli-rice",
            title = "Chicken Broccoli & Rice", description = "16 oz punnet, on sale this week.",
            tag = "Smart suggestion", tagColor = SmartPickColor.Blue,
            cta = "Add to list", action = SmartPickAction.AddToList,
            discountBadge = "2 for ${'$'}5",
            progress = SmartPickCard.Progress(SmartPickColor.Blue, 60, "Save ${'$'}1.98"),
        ),
        SmartPickCard(
            id = "cheddar-cheese", imageKey = "cheddar-cheese",
            title = "Cheddar Cheese", description = "16 oz punnet, on sale this week.",
            tag = "Weekly deal", tagColor = SmartPickColor.Red,
            cta = "View recipe", action = SmartPickAction.ViewRecipe,
            discountBadge = "2 for ${'$'}5",
            progress = SmartPickCard.Progress(SmartPickColor.Red, 60, "Save ${'$'}1.98"),
        ),
    )
}
