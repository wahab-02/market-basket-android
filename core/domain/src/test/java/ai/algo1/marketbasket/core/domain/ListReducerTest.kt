package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.list.ListReducer
import ai.algo1.marketbasket.core.domain.model.Category
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListReducerTest {
    private fun cats(vararg c: Category) = c.toList()
    private fun cat(id: String, vararg items: GroceryItem) =
        Category(id = id, name = id, color = "#000000", colorDark = "#000000", items = items.toList())
    private fun item(id: String, name: String, categoryId: String, qty: Int? = 1, checked: Boolean = false) =
        GroceryItem(id = id, name = name, categoryId = categoryId, checked = checked, quantity = qty)

    private fun find(categories: List<Category>, id: String): GroceryItem? =
        categories.flatMap { it.items }.firstOrNull { it.id == id }

    @Test fun addNewItem_insertsIntoCategory() {
        val r = ListReducer.addOrIncrement(cats(cat("dairy")), name = "Milk", categoryId = "dairy", source = "search", newId = "n1")
        assertTrue(r.inserted)
        assertEquals("n1", r.item.id)
        assertEquals(1, r.categories.single { it.id == "dairy" }.items.size)
        assertEquals("Milk", find(r.categories, "n1")!!.name)
    }

    @Test fun addNew_createsMissingCategoryWithTitleCasedName() {
        val r = ListReducer.addOrIncrement(cats(cat("dairy")), name = "Soap", categoryId = "personal-care", source = "search", newId = "n2")
        val created = r.categories.single { it.id == "personal-care" }
        assertEquals("Personal Care", created.name)
        assertEquals(1, created.items.size)
    }

    @Test fun addExisting_incrementsQuantityClampedTo99_andMergesImageKeepingExisting() {
        val start = cats(cat("dairy", item("e1", "Milk", "dairy", qty = 98, checked = true).copy(imageUrl = "old")))
        val r = ListReducer.addOrIncrement(start, name = "  milk ", categoryId = "dairy", source = "voice", imageUrl = "new", quantity = 5, newId = "ignored")
        assertFalse(r.inserted)
        val merged = find(r.categories, "e1")!!
        assertEquals(99, merged.quantity)          // min(98+5, 99)
        assertFalse(merged.checked)                // reset to unchecked
        assertEquals("voice", merged.source)       // new source wins
        assertEquals("old", merged.imageUrl)       // existing image kept
    }

    @Test fun addExisting_moveToCategory_relocatesItem() {
        val start = cats(cat("dairy", item("e1", "Milk", "dairy")), cat("deals"))
        val r = ListReducer.addOrIncrement(start, name = "Milk", categoryId = "deals", source = "promo", moveExistingToCategory = true, newId = "x")
        assertTrue(r.categories.single { it.id == "dairy" }.items.isEmpty())
        assertEquals("deals", find(r.categories, "e1")!!.categoryId)
    }

    @Test fun toggle_flipsCheckedAndReportsNewValue() {
        val r = ListReducer.toggle(cats(cat("dairy", item("e1", "Milk", "dairy", checked = false))), "e1")
        assertEquals(true, r.newChecked)
        assertTrue(find(r.categories, "e1")!!.checked)
    }

    @Test fun toggle_unknownId_isNoOpWithNullChecked() {
        val r = ListReducer.toggle(cats(cat("dairy")), "missing")
        assertNull(r.newChecked)
    }

    @Test fun delete_removesById() {
        val out = ListReducer.delete(cats(cat("dairy", item("e1", "Milk", "dairy"))), "e1")
        assertNull(find(out, "e1"))
    }

    @Test fun realtimeInsert_dedupById_isNoOp() {
        val start = cats(cat("dairy", item("e1", "Milk", "dairy", qty = 2)))
        val out = ListReducer.applyRealtimeInsert(start, item("e1", "Milk", "dairy", qty = 9))
        assertEquals(2, find(out, "e1")!!.quantity)   // unchanged
        assertEquals(1, out.flatMap { it.items }.size)
    }

    @Test fun realtimeInsert_dedupByName_mergesQuantityAndKeepsExistingImage() {
        val start = cats(cat("dairy", item("e1", "Milk", "dairy", qty = 3).copy(imageUrl = "old", source = "search")))
        val out = ListReducer.applyRealtimeInsert(start, item("other", "  MILK ", "dairy", qty = 4, checked = true).copy(imageUrl = "new", source = "whatsapp"))
        val merged = find(out, "e1")!!
        assertEquals(7, merged.quantity)         // 3 + 4
        assertEquals("whatsapp", merged.source)  // incoming source wins
        assertEquals("old", merged.imageUrl)     // existing image kept
        assertNull(find(out, "other"))           // not appended
    }

    @Test fun realtimeInsert_newItem_appendsToCategory_fallbackFirstWhenUnknown() {
        val start = cats(cat("dairy"), cat("bakery"))
        val unknown = ListReducer.applyRealtimeInsert(start, item("z", "Mystery", "frozen", qty = 1))
        assertEquals("dairy", unknown.first().items.single().categoryId)  // fell back to first category, categoryId rewritten
    }

    @Test fun realtimeUpdate_mergesFieldsById() {
        val start = cats(cat("dairy", item("e1", "Milk", "dairy", qty = 1, checked = false).copy(imageUrl = "old")))
        val out = ListReducer.applyRealtimeUpdate(start, item("e1", "Milk 2L", "dairy", qty = 4, checked = true).copy(imageUrl = null, source = "google"))
        val u = find(out, "e1")!!
        assertEquals("Milk 2L", u.name); assertEquals(4, u.quantity); assertTrue(u.checked)
        assertEquals("old", u.imageUrl)   // null incoming keeps existing
    }

    @Test fun realtimeDelete_removesById() {
        val out = ListReducer.applyRealtimeDelete(cats(cat("dairy", item("e1", "Milk", "dairy"))), "e1")
        assertNull(find(out, "e1"))
    }
}
