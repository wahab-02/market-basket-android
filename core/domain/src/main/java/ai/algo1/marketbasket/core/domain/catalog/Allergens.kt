package ai.algo1.marketbasket.core.domain.catalog

/** Pure port of ALLERGEN_KEYWORDS + parseAllergens in src/services/productSearch.ts. */
object Allergens {
    val ALLERGEN_KEYWORDS: List<String> = listOf(
        "gluten", "wheat", "barley", "rye", "oats",
        "milk", "dairy", "lactose",
        "eggs", "egg",
        "peanuts", "peanut",
        "tree nuts", "nuts", "almond", "cashew", "walnut", "hazelnut", "pecan", "pistachio",
        "soy", "soya",
        "fish", "shellfish", "crustacean",
        "sesame",
        "celery", "mustard", "lupin", "molluscs", "sulphites", "sulphur dioxide",
    )

    /** Returns the allergen keywords found as substrings, in ALLERGEN_KEYWORDS order. */
    fun parseAllergens(dietaryInfo: String): List<String> {
        val lower = dietaryInfo.lowercase()
        return ALLERGEN_KEYWORDS.filter { lower.contains(it) }
    }
}
