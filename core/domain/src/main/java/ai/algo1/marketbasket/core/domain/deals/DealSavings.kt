package ai.algo1.marketbasket.core.domain.deals

import java.util.Locale

/** Pure port of getDealSavingsAmount / getDealsSavingsTotal / formatSavingsTotal (dealsData.ts). */
object DealSavings {
    // "$2.98" -> 2.98 ; the literal $ here is not a Kotlin template ('(' follows it).
    private val dollar = Regex("\\$([0-9]+(?:\\.[0-9]+)?)")
    private val cents = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*¢")

    fun savingsAmount(savings: String): Double {
        val s = savings.trim()
        if (s.isEmpty()) return 0.0
        dollar.find(s)?.let { return it.groupValues[1].toDouble() }
        cents.find(s)?.let { return it.groupValues[1].toDouble() / 100.0 }
        return 0.0
    }

    fun savingsTotal(deals: List<Deal>): Double = deals.sumOf { savingsAmount(it.savings) }

    /** Mirrors JS `$${amount.toFixed(2)}`. */
    fun formatSavingsTotal(amount: Double): String = "${'$'}" + String.format(Locale.US, "%.2f", amount)
}
