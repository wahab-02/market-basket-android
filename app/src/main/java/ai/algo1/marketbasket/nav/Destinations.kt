package ai.algo1.marketbasket.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Filled.Home),
    List("list", "List", Icons.Filled.ShoppingCart),
    Deals("deals", "Deals", Icons.Filled.LocalOffer),
    Ideas("ideas", "Ideas", Icons.Filled.Lightbulb),
    You("you", "You", Icons.Filled.Person);

    companion object {
        val START = List
    }
}
