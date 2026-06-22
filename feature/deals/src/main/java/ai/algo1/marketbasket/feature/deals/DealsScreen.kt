package ai.algo1.marketbasket.feature.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors
import ai.algo1.marketbasket.core.domain.deals.Deal
import ai.algo1.marketbasket.core.domain.util.ItemName

private val DealRed = Color(0xFFC7353A)
private val DealsBackground = Color(0xFFF1F1F1)

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
    var selectedCategory by remember(state.sections) { mutableStateOf(state.sections.first().category) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DealsBackground),
        contentPadding = PaddingValues(bottom = 142.dp),
    ) {
        item(key = "category_chips") {
            DealsCategoryChips(
                categories = state.sections.map { it.category },
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
            )
        }
        state.sections
            .filter { section -> section.category == selectedCategory || selectedCategory.isBlank() }
            .forEach { section ->
                item(key = "h_${section.category}") {
                    Text(
                        text = section.category,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 26.dp, bottom = 22.dp),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF080816),
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

@Composable
private fun DealsCategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categories.take(8).forEach { category ->
            val selected = category == selectedCategory
            Surface(
                onClick = { onCategorySelected(category) },
                color = if (selected) DealRed else Color.White,
                shadowElevation = if (selected) 0.dp else 2.dp,
            ) {
                Text(
                    text = category,
                    color = if (selected) Color.White else Color(0xFF3D3D3D),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 12.dp),
                )
            }
        }
    }
}
