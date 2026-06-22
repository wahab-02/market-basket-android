package ai.algo1.marketbasket.feature.today

import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.catalog.CategoryResolver
import ai.algo1.marketbasket.core.domain.deals.DealSavings
import ai.algo1.marketbasket.core.domain.deals.ForYouDeals
import ai.algo1.marketbasket.core.domain.model.Category
import ai.algo1.marketbasket.core.domain.today.CommunityList
import ai.algo1.marketbasket.core.domain.today.CommunityLists
import ai.algo1.marketbasket.core.domain.today.SmartPickCard
import ai.algo1.marketbasket.core.domain.today.SmartPicks
import ai.algo1.marketbasket.core.domain.today.TripEstimate
import ai.algo1.marketbasket.core.domain.util.ItemName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val itemCount: Int = 0,
    val categoryCount: Int = 0,
    val saleCount: Int = 0,
    val saleCountLabel: String = "",
    val savingsTotal: String = "${'$'}0.00",
    val tripMinutes: Int = 1,
    val smartPicks: List<SmartPickCard> = emptyList(),
    val communityLists: List<CommunityList> = emptyList(),
    val addedItemNames: Set<String> = emptySet(),
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val listRepository: ListRepository,
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = listRepository.categories
        .map { buildState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), buildState(listRepository.categories.value))

    private fun buildState(cats: List<Category>): TodayUiState {
        val itemCount = cats.sumOf { it.items.size }
        val categoryCount = cats.count { it.items.isNotEmpty() }
        val saleCount = ForYouDeals.all.count { DealSavings.savingsAmount(it.savings) > 0 }
        return TodayUiState(
            itemCount = itemCount,
            categoryCount = categoryCount,
            saleCount = saleCount,
            saleCountLabel = "$saleCount ${if (saleCount == 1) "item" else "items"} on sale",
            savingsTotal = DealSavings.formatSavingsTotal(DealSavings.savingsTotal(ForYouDeals.all)),
            tripMinutes = TripEstimate.estimateTripMinutes(itemCount, categoryCount),
            smartPicks = SmartPicks.all,
            communityLists = CommunityLists.all,
            addedItemNames = cats.flatMap { it.items }.map { ItemName.normalize(it.name) }.toSet(),
        )
    }

    /** Port of onAddCommunityItems: resolve each name's category (fallback "produce") and add as source "community". */
    fun addCommunityItems(names: List<String>) {
        viewModelScope.launch {
            names.forEach { name ->
                val categoryId = CategoryResolver.resolveCategoryFromText(
                    rawCategory = null, productName = name, fallbackCategory = "produce",
                )
                listRepository.addOrIncrement(name = name, categoryId = categoryId, source = "community")
            }
        }
    }
}
