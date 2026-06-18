package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.catalog.CategoryMaps
import ai.algo1.marketbasket.core.domain.catalog.SeedCategories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryMapsTest {
    @Test
    fun knownCategoryIds_matchTsOrderAndValues() {
        assertEquals(
            listOf("produce", "snacks", "dairy", "bakery", "beverages", "frozen"),
            CategoryMaps.KNOWN_CATEGORY_IDS,
        )
    }

    @Test
    fun categoryStringMap_resolvesRepresentativeEntries() {
        // Spot-checks copied from src/services/categoryMaps.ts
        assertEquals("beverages", CategoryMaps.CATEGORY_STRING_MAP["drinks"])
        assertEquals("beverages", CategoryMaps.CATEGORY_STRING_MAP["juice"])
        assertEquals("bakery", CategoryMaps.CATEGORY_STRING_MAP["bread"])
    }

    @Test
    fun categoryStringMap_everyValueIsAKnownCategory() {
        CategoryMaps.CATEGORY_STRING_MAP.values.forEach { value ->
            assertTrue(
                "mapped value '$value' must be a known category id",
                value in CategoryMaps.KNOWN_CATEGORY_IDS,
            )
        }
    }

    @Test
    fun seedCategories_matchDataIndexTs() {
        assertEquals(6, SeedCategories.all.size)
        assertEquals(CategoryMaps.KNOWN_CATEGORY_IDS, SeedCategories.all.map { it.id })
        val produce = SeedCategories.all.first()
        assertEquals("Produce", produce.name)
        assertEquals("#4ecb71", produce.color)
        assertEquals("#3ba85c", produce.colorDark)
        assertTrue(produce.items.isEmpty())
    }
}
