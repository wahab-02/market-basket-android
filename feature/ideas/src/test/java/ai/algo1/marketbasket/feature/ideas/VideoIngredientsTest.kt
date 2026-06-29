package ai.algo1.marketbasket.feature.ideas

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoIngredientsTest {
    @Test
    fun `prepares video ingredients by trimming blanks and duplicate names`() {
        val ingredients = prepareVideoIngredientsForList(
            listOf(
                "  1 lb steak, cubed  ",
                "",
                "steak",
                "4 cloves Garlic, minced",
                "   ",
                "garlic",
                "Chives",
            ),
        )

        assertEquals(listOf("steak", "Garlic", "Chives"), ingredients)
    }

    @Test
    fun `prepares video ingredients by removing quantities units and serving notes`() {
        val ingredients = prepareVideoIngredientsForList(
            listOf(
                "1/2 cup heavy cream",
                "4 tbsp butter",
                "Salt and pepper to taste",
                "2 tbsp olive oil for searing",
                "Walmart seasoning (as mentioned in video)",
                "2 lbs Yukon Gold potatoes (or russet potatoes)",
            ),
        )

        assertEquals(
            listOf("heavy cream", "butter", "Salt", "pepper", "olive oil", "Walmart seasoning", "Yukon Gold potatoes"),
            ingredients,
        )
    }
}
