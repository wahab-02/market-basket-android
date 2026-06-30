package ai.algo1.marketbasket.feature.ideas

import ai.algo1.marketbasket.core.domain.model.Category
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeShoppingListMatcherTest {

    @Test
    fun `marks ingredient present when shopping list name is similar`() {
        val recipe = recipeWithIngredients(
            RecipeIngredient("Salmon Fillets", "2 pieces"),
            RecipeIngredient("Sriracha Sauce", "2 tbsp"),
        )
        val categories = categoriesWithItems("salmon", "hot sauce")

        val matched = recipe.withShoppingListMatches(categories)

        assertTrue(matched.ingredients.first { it.name == "Salmon Fillets" }.inList)
        assertFalse(matched.ingredients.first { it.name == "Sriracha Sauce" }.inList)
    }

    @Test
    fun `missing ingredients only returns items not found by similarity`() {
        val recipe = recipeWithIngredients(
            RecipeIngredient("Fresh Lime", "1 whole"),
            RecipeIngredient("Garlic Cloves", "3 cloves"),
            RecipeIngredient("Honey", "1 tbsp"),
        )
        val categories = categoriesWithItems("lime", "garlic")

        val missing = recipe.missingIngredientsFor(categories)

        assertEquals(listOf(RecipeIngredient("Honey", "1 tbsp")), missing)
    }

    private fun recipeWithIngredients(vararg ingredients: RecipeIngredient): RecipeCardItem =
        RecipeCardItem(
            id = "test",
            matchPercent = 0,
            headerTitle = "Test",
            fullTitle = "Test Recipe",
            imageRes = 0,
            timeMinutes = 1,
            calories = 1,
            difficulty = "Easy",
            tags = emptyList(),
            ingredients = ingredients.toList(),
        )

    private fun categoriesWithItems(vararg names: String): List<Category> =
        listOf(
            Category(
                id = "test",
                name = "Test",
                color = "",
                colorDark = "",
                items = names.mapIndexed { index, name ->
                    GroceryItem(id = "$index", name = name, categoryId = "test")
                },
            ),
        )
}
