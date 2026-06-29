package ai.algo1.marketbasket.feature.ideas

import ai.algo1.marketbasket.core.domain.util.ItemName

private val QuantityPrefix = Regex("""^(\d+([./]\d+)?|\d+\s+\d+/\d+)\s+""")
private val Parenthetical = Regex("""\s*\([^)]*\)""")
private val TrailingPreparation = Regex(
    pattern = """\s+(to taste|for serving|for searing|for garnish|as needed)$""",
    option = RegexOption.IGNORE_CASE,
)
private val QuantityWords = setOf(
    "cup", "cups", "tbsp", "tablespoon", "tablespoons", "tsp", "teaspoon", "teaspoons",
    "lb", "lbs", "pound", "pounds", "oz", "ounce", "ounces",
    "clove", "cloves", "can", "cans", "piece", "pieces", "whole",
    "large", "medium", "small",
)

internal fun prepareVideoIngredientsForList(ingredients: List<String>): List<String> {
    val seenNames = mutableSetOf<String>()
    return ingredients.flatMap { ingredient ->
        cleanVideoIngredientName(ingredient)
    }.mapNotNull { cleanName ->
        val itemKey = ItemName.normalize(cleanName)
        cleanName.takeIf { it.isNotEmpty() && seenNames.add(itemKey) }
    }
}

private fun cleanVideoIngredientName(ingredient: String): List<String> {
    val withoutNotes = ingredient
        .trim()
        .replace(Parenthetical, "")
        .substringBefore(",")
        .replace(TrailingPreparation, "")
        .trim()

    if (withoutNotes.isEmpty()) return emptyList()

    if (ItemName.normalize(withoutNotes) == "salt and pepper") {
        return listOf("Salt", "pepper")
    }

    val withoutAmount = withoutNotes.replace(QuantityPrefix, "")
    val words = withoutAmount.split(Regex("""\s+""")).dropWhile { it.lowercase() in QuantityWords }
    return listOf(words.joinToString(" ").trim()).filter { it.isNotEmpty() }
}
