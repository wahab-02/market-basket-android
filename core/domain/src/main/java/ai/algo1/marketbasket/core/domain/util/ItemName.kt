package ai.algo1.marketbasket.core.domain.util

/** Pure port of normalizeItemName from App.tsx / api/market-basket/import.ts. */
object ItemName {
    fun normalize(name: String): String =
        name.trim().replace(Regex("""\s+"""), " ").lowercase()
}
