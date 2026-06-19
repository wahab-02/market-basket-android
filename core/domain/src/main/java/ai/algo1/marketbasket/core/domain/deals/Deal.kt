package ai.algo1.marketbasket.core.domain.deals

/** Mirrors the TS DealRow in src/features/deals/dealsData.ts. price/savings are display strings. */
data class Deal(
    val itemName: String,
    val price: String,
    val description: String,
    val savings: String,
    val category: String,
    val image: String? = null,
)
