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
        assertEquals("Produce", SeedCategories.all.first().name)
        // All 6 categories' id + hex colors, 1:1 with src/data/index.ts
        assertEquals(
            listOf(
                Triple("produce", "#4ecb71", "#3ba85c"),
                Triple("snacks", "#e74c3c", "#c0392b"),
                Triple("dairy", "#3498db", "#2980b9"),
                Triple("bakery", "#e67e22", "#d35400"),
                Triple("beverages", "#9b59b6", "#8e44ad"),
                Triple("frozen", "#1abc9c", "#16a085"),
            ),
            SeedCategories.all.map { Triple(it.id, it.color, it.colorDark) },
        )
        assertTrue(SeedCategories.all.all { it.items.isEmpty() })
    }
}
