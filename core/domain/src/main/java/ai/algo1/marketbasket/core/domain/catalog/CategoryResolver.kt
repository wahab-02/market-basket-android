package ai.algo1.marketbasket.core.domain.catalog

/**
 * Pure port of the category-resolution logic in src/services/productSearch.ts
 * (matchToKnown / resolveCategoryFromText + the private slugify / keyword helpers).
 */
object CategoryResolver {

    /** Port of matchToKnown: explicit map -> normalized-id exact -> substring -> null. */
    fun matchToKnown(raw: String): String? {
        val lower = raw.lowercase().trim()
        // TS used a truthiness guard (`if (CATEGORY_STRING_MAP[lower])`), which also skips "".
        CategoryMaps.CATEGORY_STRING_MAP[lower]?.takeIf { it.isNotEmpty() }?.let { return it }

        val normalized = lower.replace(Regex("""\s+"""), "-").replace(Regex("""[^a-z0-9-]"""), "")
        if (normalized in CategoryMaps.KNOWN_CATEGORY_IDS) return normalized

        for (id in CategoryMaps.KNOWN_CATEGORY_IDS) {
            if (normalized.contains(id)) return id
        }
        return null
    }

    /** Port of resolveCategoryFromText: raw match -> keyword match on name -> slug(raw) or fallback. */
    fun resolveCategoryFromText(
        rawCategory: String?,
        productName: String,
        fallbackCategory: String = "other",
    ): String {
        val raw = rawCategory?.trim()
        if (!raw.isNullOrEmpty()) {
            matchToKnown(raw)?.let { return it }
        }

        for ((category, keywords) in CategoryMaps.CATEGORY_KEYWORDS) {
            if (keywords.any { textIncludesKeyword(productName, it) }) return category
        }

        return if (!raw.isNullOrEmpty()) slugifyCategory(raw) else fallbackCategory
    }

    private fun slugifyCategory(raw: String): String =
        raw.lowercase().replace(Regex("""\s+"""), "-").replace(Regex("""[^a-z0-9-]"""), "")

    /** Word-boundary-ish keyword match, mirroring the TS regex (^|[^a-z0-9])kw([^a-z0-9]|$). */
    private fun textIncludesKeyword(text: String, keyword: String): Boolean {
        val normalizedText = text.lowercase()
        val normalizedKeyword = keyword.lowercase().trim()
        if (normalizedKeyword.isEmpty()) return false
        val pattern = "(^|[^a-z0-9])" + Regex.escape(normalizedKeyword) + "([^a-z0-9]|$)"
        return Regex(pattern).containsMatchIn(normalizedText)
    }
}
