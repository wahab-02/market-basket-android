package ai.algo1.marketbasket.core.domain.util

/** Pure port of similarity() in src/services/productCategorySearch.ts. */
object StringSimilarity {
    const val SIMILARITY_THRESHOLD = 0.5

    fun similarity(search: String, categoryName: String): Double {
        val a = search.lowercase().trim()
        val b = categoryName.lowercase().trim()
        if (a == b) return 1.0
        if (b.contains(a) || a.contains(b)) return 0.85

        val aWords = a.split(Regex("""\s+""")).filter { it.isNotEmpty() }.toSet()
        val bWords = b.split(Regex("""\s+""")).filter { it.isNotEmpty() }.toSet()
        val intersection = aWords.count { it in bWords }
        val union = (aWords + bWords).size
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }
}
