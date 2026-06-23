package ai.algo1.marketbasket.nav

import ai.algo1.marketbasket.R
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

    @Test
    fun bottomNavUsesSourceAssetsForDealsAndIdeas() {
        assertEquals(R.drawable.deals, sourceBottomNavImageRes(Destination.Deals))
        assertEquals(R.drawable.recipes, sourceBottomNavImageRes(Destination.Ideas))
    }
}
