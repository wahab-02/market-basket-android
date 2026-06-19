package ai.algo1.marketbasket.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.catalog.CategoryResolver
import ai.algo1.marketbasket.core.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListUiState(val categories: List<Category> = emptyList())

@HiltViewModel
class ListViewModel @Inject constructor(
    private val listRepository: ListRepository,
) : ViewModel() {

    val uiState: StateFlow<ListUiState> = listRepository.categories
        .map { cats -> ListUiState(cats.filter { it.items.isNotEmpty() }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListUiState())

    /** Add by typed name: resolve the category from the name, then add/increment via the repository. */
    fun addByName(name: String) {
        if (name.isBlank()) return
        val categoryId = CategoryResolver.resolveCategoryFromText(rawCategory = null, productName = name)
        viewModelScope.launch { listRepository.addOrIncrement(name = name, categoryId = categoryId, source = "search") }
    }

    fun toggle(id: String) {
        viewModelScope.launch { listRepository.toggle(id) }
    }

    fun delete(id: String) {
        viewModelScope.launch { listRepository.delete(id) }
    }
}
