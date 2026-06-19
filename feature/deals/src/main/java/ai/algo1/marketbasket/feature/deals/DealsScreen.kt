package ai.algo1.marketbasket.feature.deals

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors
import ai.algo1.marketbasket.core.domain.deals.Deal
import ai.algo1.marketbasket.core.domain.util.ItemName

@Composable
fun DealsRoute(viewModel: DealsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DealsScreen(state = state, onAdd = viewModel::addToList)
}

@Composable
fun DealsScreen(state: DealsUiState, onAdd: (Deal) -> Unit) {
    if (state.sections.isEmpty()) {
        Text(
            "No deals available",
            modifier = Modifier.fillMaxSize().wrapContentSize(),
            color = MarketBasketColors.TextSecondary,
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        state.sections.forEach { section ->
            item(key = "h_${section.category}") {
                Text(
                    text = section.category,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MarketBasketColors.Primary,
                )
            }
            items(section.deals, key = { "${section.category}::${it.itemName}" }) { deal ->
                DealCard(
                    deal = deal,
                    isAdded = ItemName.normalize(deal.itemName) in state.addedNames,
                    onAdd = { onAdd(deal) },
                )
            }
        }
    }
}
