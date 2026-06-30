package ai.algo1.marketbasket.feature.ideas

import ai.algo1.marketbasket.core.domain.model.Category
import ai.algo1.marketbasket.core.domain.util.ItemName
import ai.algo1.marketbasket.core.domain.util.StringSimilarity

internal fun RecipeCardItem.withShoppingListMatches(categories: List<Category>): RecipeCardItem =
    copy(
        ingredients = ingredients.map { ingredient ->
            ingredient.copy(inList = ingredient.isInShoppingList(categories))
        },
    )

internal fun RecipeCardItem.missingIngredientsFor(categories: List<Category>): List<RecipeIngredient> =
    ingredients.filterNot { it.isInShoppingList(categories) }

private fun RecipeIngredient.isInShoppingList(categories: List<Category>): Boolean {
    val ingredientName = ItemName.normalize(name)
    return categories.asSequence()
        .flatMap { it.items.asSequence() }
        .any { item ->
            val itemName = ItemName.normalize(item.name)
            ingredientName == itemName ||
                StringSimilarity.similarity(ingredientName, itemName) >= StringSimilarity.SIMILARITY_THRESHOLD
        }
}
