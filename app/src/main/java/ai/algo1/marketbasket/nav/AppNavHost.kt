package ai.algo1.marketbasket.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.BackHandler
import ai.algo1.marketbasket.feature.chat.ChatScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val listSearchOpen = remember { mutableStateOf(false) }
    val current by navController.currentBackStackEntryAsState()
    val selectedDestination = Destination.entries.firstOrNull { dest ->
        current?.destination?.hierarchy?.any { it.route == dest.route } == true
    } ?: Destination.START
    val openListSearch = {
        listSearchOpen.value = true
        navController.navigateTab(Destination.List)
    }

    var chatOpen by remember { mutableStateOf(false) }
    BackHandler(enabled = chatOpen) { chatOpen = false }

    LaunchedEffect(selectedDestination) {
        if (selectedDestination != Destination.List) {
            listSearchOpen.value = false
        }
    }

    Box(Modifier.fillMaxSize().background(marketBasketAppBackground)) {
        Scaffold(
            containerColor = marketBasketAppBackground,
            topBar = {
                when {
                    selectedDestination.showsMarketBasketHeader() -> {
                        MarketBasketHeader(
                            onSearchClick = openListSearch,
                        )
                    }
                    selectedDestination.showsCompactHeader() -> {
                        MarketBasketCompactHeader(onSearchClick = openListSearch)
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Destination.START.route,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (selectedDestination != Destination.Ideas) Modifier.padding(padding) else Modifier),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                Destination.entries.forEach { dest ->
                    composable(dest.route) {
                        when (dest) {
                            Destination.Today -> ai.algo1.marketbasket.feature.today.TodayRoute(
                                onOpenList = { navController.navigateTab(Destination.List) },
                                onOpenDeals = { navController.navigateTab(Destination.Deals) },
                            )
                            Destination.List -> ai.algo1.marketbasket.feature.list.ListRoute(
                                searchOpen = listSearchOpen.value,
                                onSearchClose = { listSearchOpen.value = false },
                            )
                            Destination.Deals -> ai.algo1.marketbasket.feature.deals.DealsRoute()
                            Destination.You -> ai.algo1.marketbasket.feature.onboarding.ConnectionRoute()
                            Destination.Ideas -> ai.algo1.marketbasket.feature.ideas.IdeasRoute()
                        }
                    }
                }
            }
        }

        FloatingBottomNav(
            selected = selectedDestination,
            onDestinationClick = { dest ->
                if (dest != Destination.List) {
                    listSearchOpen.value = false
                }
                navController.navigateTab(dest)
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            inverted = selectedDestination == Destination.Ideas,
        )
        if (selectedDestination != Destination.Ideas) {
            FloatingChatLauncher(
                onClick = { chatOpen = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 28.dp, bottom = 104.dp),
            )
        }

        if (chatOpen) {
            ChatScreen(onClose = { chatOpen = false })
        }

        // HomeIntroOverlay is intentionally disabled for now; keep the component for later.
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Text(text = label, modifier = Modifier.fillMaxSize().wrapContentSize())
}

private fun androidx.navigation.NavController.navigateTab(dest: Destination) {
    navigate(dest.route) {
        popUpTo(Destination.START.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
