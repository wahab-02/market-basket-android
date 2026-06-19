package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.deals.MarketBasketDeals
import ai.algo1.marketbasket.core.domain.deals.PromoCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromoCategoryTest {
    @Test fun isGeneric_forYouAndFeatured() {
        assertTrue(PromoCategory.isGeneric("For You"))
        assertTrue(PromoCategory.isGeneric("Featured Items"))
        assertFalse(PromoCategory.isGeneric("Produce"))
        assertFalse(PromoCategory.isGeneric(null))
    }

    @Test fun generic_resolvesByProductNameKeyword() {
        // "For You" is generic -> rawCategory ignored, resolve from the name's keywords
        assertEquals("dairy", PromoCategory.resolveListCategory("For You", "Whole Milk"))
        // "Frozen Pizza": no produce/dairy/bakery/snacks/beverages keyword matches; frozen's "frozen"/"pizza" do.
        assertEquals("frozen", PromoCategory.resolveListCategory("Featured Items", "Frozen Pizza"))
    }

    @Test fun specificCategory_matchedToKnown() {
        assertEquals("produce", PromoCategory.resolveListCategory("Produce", "Bananas"))
        assertEquals("bakery", PromoCategory.resolveListCategory("Fresh Bakery", "Sourdough"))
    }

    @Test fun dealsData_isPopulatedAndHasForYou() {
        assertTrue(MarketBasketDeals.all.size > 50)
        assertTrue(MarketBasketDeals.all.any { it.category == "For You" })
        assertFalse(MarketBasketDeals.all.any { it.itemName.isBlank() })
    }

    @Test fun dealSlug_slugifiesCategoryAndName() {
        val deal = MarketBasketDeals.all.first()
        val slug = MarketBasketDeals.dealSlug(deal)
        assertTrue(slug.matches(Regex("[a-z0-9-]+")))
        assertTrue(slug.contains("-"))
    }
}
