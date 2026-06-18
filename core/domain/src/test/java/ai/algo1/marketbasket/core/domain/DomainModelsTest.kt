package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.model.Category
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class DomainModelsTest {
    @Test
    fun groceryItem_defaults_matchTsOptionalSemantics() {
        val item = GroceryItem(id = "1", name = "Milk", categoryId = "dairy")
        assertFalse(item.checked)
        assertNull(item.quantity)
        assertNull(item.source)
        assertNull(item.imageUrl)
        assertNull(item.addedByName)
    }

    @Test
    fun category_defaultsToEmptyItems() {
        val category = Category(id = "produce", name = "Produce", color = "#4ecb71", colorDark = "#3ba85c")
        assertEquals(emptyList<GroceryItem>(), category.items)
    }
}
