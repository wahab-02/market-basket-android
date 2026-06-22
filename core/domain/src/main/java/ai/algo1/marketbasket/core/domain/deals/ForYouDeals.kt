package ai.algo1.marketbasket.core.domain.deals

/** Port of FOR_YOU_DEALS (src/features/deals/dealsData.ts) — the 3-item subset Today uses for savings. */
object ForYouDeals {
    val all: List<Deal> = listOf(
        Deal(itemName = "Fresh Romaine Lettuce", price = "2 for ${'$'}3", description = "Crisp romaine hearts for salads, wraps, and burger night.", savings = "${'$'}2.98", category = "For You", image = "https://raw.githubusercontent.com/umar-rmg/market-basket-product-icons/main/images/fresh-romaine-lettuce.png"),
        Deal(itemName = "Lay's Potato Chips 5-8 oz.", price = "2 for ${'$'}6", description = "5-8 oz. bags in assorted varieties.", savings = "${'$'}2.58", category = "For You", image = "https://raw.githubusercontent.com/umar-rmg/market-basket-product-icons/main/images/lay-s-potato-chips-5-8-oz.png"),
        Deal(itemName = "Utopihen Pasture Raised Large Brown Eggs", price = "3.99", description = "1 dozen pasture raised large brown eggs.", savings = "${'$'}1.70", category = "For You", image = "https://raw.githubusercontent.com/umar-rmg/market-basket-product-icons/main/images/utopihen-pasture-raised-large-brown-eggs.png"),
    )
}
