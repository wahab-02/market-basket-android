package ai.algo1.marketbasket.core.domain.list

import ai.algo1.marketbasket.core.domain.model.Category
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import ai.algo1.marketbasket.core.domain.util.ItemName

/**
 * Pure port of the list mutation + realtime reconciliation logic from App.tsx's ShoppingList.
 * Operates on an immutable List<Category>; callers persist the side effects separately.
 */
object ListReducer {

    fun getItemQuantity(item: GroceryItem): Int =
        item.quantity?.takeIf { it > 0 } ?: 1

    data class AddResult(val categories: List<Category>, val item: GroceryItem, val inserted: Boolean)
    data class ToggleResult(val categories: List<Category>, val newChecked: Boolean?)

    fun addOrIncrement(
        categories: List<Category>,
        name: String,
        categoryId: String,
        source: String?,
        imageUrl: String? = null,
        quantity: Int = 1,
        moveExistingToCategory: Boolean = false,
        newId: String,
    ): AddResult {
        val cleanName = name.trim()
        val amount = quantity.coerceIn(1, 99)
        val existing = findByName(categories, cleanName)

        if (existing != null) {
            val nextQuantity = minOf(getItemQuantity(existing) + amount, 99)
            val target = if (moveExistingToCategory) categoryId else existing.categoryId
            val updated = existing.copy(
                categoryId = target,
                checked = false,
                quantity = nextQuantity,
                source = source,
                imageUrl = existing.imageUrl ?: imageUrl,
            )
            val pruned = categories.map { c -> c.copy(items = c.items.filter { it.id != existing.id }) }
            return AddResult(appendTo(pruned, target, updated), updated, inserted = false)
        }

        val newItem = GroceryItem(
            id = newId,
            name = cleanName,
            categoryId = categoryId,
            checked = false,
            quantity = amount,
            source = source,
            imageUrl = imageUrl,
        )
        return AddResult(appendTo(categories, categoryId, newItem), newItem, inserted = true)
    }

    fun toggle(categories: List<Category>, itemId: String): ToggleResult {
        val current = categories.flatMap { it.items }.firstOrNull { it.id == itemId }
            ?: return ToggleResult(categories, null)
        val newChecked = !current.checked
        val out = categories.map { c ->
            c.copy(items = c.items.map { if (it.id == itemId) it.copy(checked = newChecked) else it })
        }
        return ToggleResult(out, newChecked)
    }

    fun delete(categories: List<Category>, itemId: String): List<Category> =
        categories.map { c -> c.copy(items = c.items.filter { it.id != itemId }) }

    /** Realtime UPDATE: merge fields into the item with the same id (no reorder). */
    fun applyRealtimeUpdate(categories: List<Category>, item: GroceryItem): List<Category> =
        categories.map { c ->
            c.copy(items = c.items.map { existing ->
                if (existing.id == item.id) existing.copy(
                    checked = item.checked,
                    name = item.name,
                    quantity = item.quantity ?: 1,
                    source = item.source,
                    imageUrl = item.imageUrl ?: existing.imageUrl,
                ) else existing
            })
        }

    /** Realtime INSERT: dedup by id -> by normalized name (merge) -> append (fallback first category). */
    fun applyRealtimeInsert(categories: List<Category>, item: GroceryItem): List<Category> {
        if (categories.any { c -> c.items.any { it.id == item.id } }) return categories

        val existing = findByName(categories, item.name)
        if (existing != null) {
            return categories.map { c ->
                c.copy(items = c.items.map { e ->
                    if (e.id == existing.id) e.copy(
                        checked = item.checked,
                        quantity = minOf(getItemQuantity(e) + (item.quantity ?: 1), 99),
                        source = item.source ?: e.source,
                        imageUrl = e.imageUrl ?: item.imageUrl,
                    ) else e
                })
            }
        }

        if (categories.isEmpty()) return categories
        val targetIndex = categories.indexOfFirst { it.id == item.categoryId }.let { if (it < 0) 0 else it }
        val targetId = categories[targetIndex].id
        return categories.mapIndexed { i, c ->
            if (i == targetIndex) c.copy(items = c.items + item.copy(categoryId = targetId)) else c
        }
    }

    fun applyRealtimeDelete(categories: List<Category>, itemId: String): List<Category> =
        delete(categories, itemId)

    // ── helpers ──
    private fun findByName(categories: List<Category>, name: String): GroceryItem? {
        val target = ItemName.normalize(name)
        return categories.flatMap { it.items }.firstOrNull { ItemName.normalize(it.name) == target }
    }

    private fun appendTo(categories: List<Category>, categoryId: String, item: GroceryItem): List<Category> {
        val idx = categories.indexOfFirst { it.id == categoryId }
        return if (idx >= 0) {
            categories.mapIndexed { i, c -> if (i == idx) c.copy(items = c.items + item) else c }
        } else {
            categories + CategoryFactory.create(categoryId).copy(items = listOf(item))
        }
    }
}
