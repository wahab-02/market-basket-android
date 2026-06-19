package ai.algo1.marketbasket.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val current by navController.currentBackStackEntryAsState()
            NavigationBar {
                Destination.entries.forEach { dest ->
                    val selected = current?.destination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(Destination.START.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.START.route,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Destination.entries.forEach { dest ->
                composable(dest.route) {
                    when (dest) {
                        Destination.List -> ai.algo1.marketbasket.feature.list.ListRoute()
                        Destination.Deals -> ai.algo1.marketbasket.feature.deals.DealsRoute()
                        else -> PlaceholderScreen(dest.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Text(text = label, modifier = Modifier.fillMaxSize().wrapContentSize())
}
