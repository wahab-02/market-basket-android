package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.deals.DealSavings
import ai.algo1.marketbasket.core.domain.deals.ForYouDeals
import org.junit.Assert.assertEquals
import org.junit.Test

class DealSavingsTest {
    @Test fun savingsAmount_dollar() = assertEquals(2.98, DealSavings.savingsAmount("\$2.98"), 0.0001)
    @Test fun savingsAmount_cents() = assertEquals(0.90, DealSavings.savingsAmount("90¢"), 0.0001)
    @Test fun savingsAmount_empty() = assertEquals(0.0, DealSavings.savingsAmount(""), 0.0001)
    @Test fun savingsAmount_blank() = assertEquals(0.0, DealSavings.savingsAmount("   "), 0.0001)
    @Test fun savingsAmount_noNumber() = assertEquals(0.0, DealSavings.savingsAmount("Save big"), 0.0001)
    @Test fun savingsAmount_dollarValue() = assertEquals(1.40, DealSavings.savingsAmount("\$1.40"), 0.0001)

    @Test fun forYouDeals_areThree() = assertEquals(3, ForYouDeals.all.size)
    @Test fun savingsTotal_forYou_is_7_26() =
        assertEquals(7.26, DealSavings.savingsTotal(ForYouDeals.all), 0.0001)
    @Test fun formatSavingsTotal_twoDecimals() =
        assertEquals("\$7.26", DealSavings.formatSavingsTotal(7.26))
    @Test fun formatSavingsTotal_padsZeros() =
        assertEquals("\$3.50", DealSavings.formatSavingsTotal(3.5))
    @Test fun saleCount_forYou_allHaveSavings() =
        assertEquals(3, ForYouDeals.all.count { DealSavings.savingsAmount(it.savings) > 0 })
}
