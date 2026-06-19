package ai.algo1.marketbasket.core.domain.deals

import ai.algo1.marketbasket.core.domain.catalog.CategoryResolver
import ai.algo1.marketbasket.core.domain.util.ItemName

/** Pure port of isGenericDealCategory + resolvePromoListCategory from App.tsx. */
object PromoCategory {
    fun isGeneric(category: String?): Boolean {
        val normalized = (category?.trim()?.lowercase() ?: "").replace(Regex("""\s+"""), "-")
        return normalized == "for-you" || normalized == "featured-items"
    }

    fun resolveListCategory(
        rawCategory: String?,
        productName: String,
        detailText: String = "",
        fallback: String = "other",
    ): String {
        val categoryText = listOf(productName, detailText).filter { it.isNotBlank() }.joinToString(" ")
        val generic = isGeneric(rawCategory)
        val resolved = CategoryResolver.resolveCategoryFromText(
            rawCategory = if (generic) null else rawCategory,
            productName = categoryText,
            fallbackCategory = if (generic) "" else fallback,
        )
        if (resolved.isNotEmpty()) return resolved

        val specific = MarketBasketDeals.all.firstOrNull {
            ItemName.normalize(it.itemName) == ItemName.normalize(productName) && !isGeneric(it.category)
        }?.category
        return CategoryResolver.resolveCategoryFromText(specific, categoryText, fallback)
    }
}
