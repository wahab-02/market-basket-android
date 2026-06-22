package ai.algo1.marketbasket.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.algo1.marketbasket.core.domain.today.CommunityList
import ai.algo1.marketbasket.core.domain.today.SmartPickAction
import ai.algo1.marketbasket.feature.today.cards.CommunitySuggestionCard
import ai.algo1.marketbasket.feature.today.cards.CreateCommunityListCard
import ai.algo1.marketbasket.feature.today.cards.DealsSavingsCard
import ai.algo1.marketbasket.feature.today.cards.ListStatsCard
import ai.algo1.marketbasket.feature.today.cards.TodayRecipeCard
import ai.algo1.marketbasket.feature.today.cards.TodaySmartPickCard

@Composable
fun TodayRoute(
    viewModel: TodayViewModel = hiltViewModel(),
    onOpenList: () -> Unit,
    onOpenDeals: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreen(
        state = state,
        onOpenList = onOpenList,
        onOpenDeals = onOpenDeals,
        onOpenFeaturedRecipe = { /* deferred: recipes (Plan 11) */ },
        onOpenSmartPickRecipe = { /* deferred: recipes (Plan 11) */ },
        onAddSmartPick = { /* deferred: recipe-ingredient add (Plan 11) */ },
        onAddCommunityItems = viewModel::addCommunityItems,
    )
}

@Composable
fun TodayScreen(
    state: TodayUiState,
    onOpenList: () -> Unit,
    onOpenDeals: () -> Unit,
    onOpenFeaturedRecipe: () -> Unit,
    onOpenSmartPickRecipe: (String) -> Unit,
    onAddSmartPick: (String) -> Unit,
    onAddCommunityItems: (List<String>) -> Unit,
) {
    var activeList by remember { mutableStateOf<CommunityList?>(null) }

    Column(
        Modifier.fillMaxSize().background(TodayColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 142.dp),
    ) {
        DealsSavingsCard(state.savingsTotal, state.saleCountLabel, onClick = onOpenDeals)
        ListStatsCard(state.itemCount, state.categoryCount, state.saleCount, state.tripMinutes, onClick = onOpenList)
        TodayRecipeCard(onClick = onOpenFeaturedRecipe)

        SectionHeader("Smart picks for you", "Personalized", Modifier.padding(top = 32.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            state.smartPicks.forEach { card ->
                val onClick: () -> Unit = if (card.action == SmartPickAction.AddToList) {
                    { onAddSmartPick(card.id) }
                } else {
                    { onOpenSmartPickRecipe(card.id) }
                }
                Column(Modifier.clickable(onClick = onClick)) {
                    TodaySmartPickCard(card = card, onAddToList = { onAddSmartPick(card.id) }, onViewRecipe = { onOpenSmartPickRecipe(card.id) })
                }
            }
        }

        SectionHeader("Community", "Suggestions", Modifier.padding(top = 36.dp))
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            state.communityLists.forEach { list ->
                CommunitySuggestionCard(list = list, onShop = { activeList = list })
            }
        }

        CreateCommunityListCard()
    }

    activeList?.let { list ->
        CommunityListSheet(
            list = list,
            addedItemNames = state.addedItemNames,
            onAddItems = onAddCommunityItems,
            onDismiss = { activeList = null },
        )
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title.uppercase(), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TodayColors.Ink)
        Text(trailing.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TodayColors.Ink.copy(alpha = 0.36f))
    }
}
