package ai.algo1.marketbasket.feature.deals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.deals.Deal
import ai.algo1.marketbasket.core.domain.deals.MarketBasketDeals
import ai.algo1.marketbasket.core.domain.deals.PromoCategory
import ai.algo1.marketbasket.core.domain.util.ItemName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DealSection(val category: String, val deals: List<Deal>)
data class DealsUiState(val sections: List<DealSection>, val addedNames: Set<String> = emptySet())

@HiltViewModel
class DealsViewModel @Inject constructor(
    private val listRepository: ListRepository,
) : ViewModel() {

    private val sections: List<DealSection> =
        MarketBasketDeals.all.groupBy { it.category }.map { (category, deals) -> DealSection(category, deals) }

    val uiState: StateFlow<DealsUiState> = listRepository.categories
        .map { cats ->
            DealsUiState(sections, cats.flatMap { it.items }.map { ItemName.normalize(it.name) }.toSet())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DealsUiState(sections))

    fun addToList(deal: Deal) {
        val categoryId = PromoCategory.resolveListCategory(deal.category, deal.itemName, deal.description)
        viewModelScope.launch {
            listRepository.addOrIncrement(
                name = deal.itemName,
                categoryId = categoryId,
                source = "promo",
                imageUrl = deal.image,
                moveExistingToCategory = PromoCategory.isGeneric(deal.category),
            )
        }
    }
}
