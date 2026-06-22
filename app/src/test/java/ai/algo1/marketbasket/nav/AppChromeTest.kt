package ai.algo1.marketbasket.nav

import org.junit.Assert.assertEquals
import org.junit.Test

class AppChromeTest {
    @Test
    fun bottomNavMatchesMarketBasketSourceShell() {
        assertEquals(
            listOf(Destination.Today, Destination.List, Destination.Deals, Destination.Ideas),
            primaryBottomNavDestinations,
        )
        assertEquals(Destination.You, profileBottomNavDestination)
    }
}
