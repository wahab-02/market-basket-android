package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.catalog.CategoryResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryResolverTest {
    @Test fun matchToKnown_explicitStringMap() {
        assertEquals("beverages", CategoryResolver.matchToKnown("drinks"))
    }

    @Test fun matchToKnown_normalizedExactId() {
        assertEquals("produce", CategoryResolver.matchToKnown("Produce"))
    }

    @Test fun matchToKnown_substringOfKnownId() {
        // "Fresh Bakery Items" -> "fresh-bakery-items" contains "bakery"
        assertEquals("bakery", CategoryResolver.matchToKnown("Fresh Bakery Items"))
    }

    @Test fun matchToKnown_noMatchReturnsNull() {
        assertNull(CategoryResolver.matchToKnown("Electronics"))
    }

    @Test fun resolve_rawCategoryWins() {
        assertEquals("beverages", CategoryResolver.resolveCategoryFromText("Drinks", "Coca Cola"))
    }

    @Test fun resolve_keywordOnProductName_dairy() {
        // no raw category -> keyword scan of name; CATEGORY_KEYWORDS["dairy"] has "milk"
        assertEquals("dairy", CategoryResolver.resolveCategoryFromText(null, "Fresh Whole Milk"))
    }

    @Test fun resolve_keywordOnProductName_frozen() {
        // CATEGORY_KEYWORDS["frozen"] has "pizza"; earlier categories don't match
        assertEquals("frozen", CategoryResolver.resolveCategoryFromText(null, "Margherita Pizza"))
    }

    @Test fun resolve_unknownRawSlugified() {
        assertEquals("zzz-unknown", CategoryResolver.resolveCategoryFromText("Zzz Unknown", "qwxyz"))
    }

    @Test fun resolve_fallbackWhenNothingMatches() {
        assertEquals("other", CategoryResolver.resolveCategoryFromText(null, "qwxyz"))
        assertEquals("misc", CategoryResolver.resolveCategoryFromText(null, "qwxyz", "misc"))
    }
}
