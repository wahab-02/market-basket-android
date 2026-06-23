package ai.algo1.marketbasket.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.repository.CatalogRepository
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.catalog.CategoryResolver
import ai.algo1.marketbasket.core.domain.model.CatalogProduct
import ai.algo1.marketbasket.core.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListUiState(
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<CatalogProduct> = emptyList(),
    val isSearching: Boolean = false,
)

@HiltViewModel
class ListViewModel @Inject constructor(
    private val listRepository: ListRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<CatalogProduct>>(emptyList())
    private val _isSearching = MutableStateFlow(false)
    private val enrichedIds = mutableSetOf<String>()

    val uiState: StateFlow<ListUiState> = combine(
        listRepository.categories.map { cats -> cats.filter { it.items.isNotEmpty() } },
        _searchQuery,
        _searchResults,
        _isSearching,
    ) { cats, query, results, searching ->
        ListUiState(categories = cats, searchQuery = query, searchResults = results, isSearching = searching)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListUiState())

    init {
        viewModelScope.launch {
            _searchQuery.debounce(400L).collectLatest { q ->
                val trimmed = q.trim()
                if (trimmed.isEmpty()) {
                    _searchResults.value = emptyList()
                    _isSearching.value = false
                    return@collectLatest
                }
                _isSearching.value = true
                try {
                    _searchResults.value = catalogRepository.searchProducts(trimmed)
                } catch (_: Exception) {
                    // best-effort
                } finally {
                    _isSearching.value = false
                }
            }
        }

        viewModelScope.launch {
            listRepository.categories.collect { cats ->
                val needsImage = cats.flatMap { it.items }
                    .filter { it.imageUrl == null && it.id !in enrichedIds }
                for (item in needsImage) {
                    enrichedIds.add(item.id)
                    launch {
                        try {
                            val imageUrl = catalogRepository.searchCategoryImage(item.name)
                                ?: catalogRepository.searchProducts(item.name).firstOrNull()?.imageUrl
                                ?: return@launch
                            listRepository.setImageUrl(item.id, imageUrl)
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun addByName(name: String) {
        if (name.isBlank()) return
        val categoryId = CategoryResolver.resolveCategoryFromText(rawCategory = null, productName = name)
        viewModelScope.launch {
            listRepository.addOrIncrement(name = name, categoryId = categoryId, source = "search")
        }
    }

    fun addFromCatalog(product: CatalogProduct) {
        viewModelScope.launch {
            listRepository.addOrIncrement(
                name = product.name,
                categoryId = product.category,
                source = "search",
                imageUrl = product.imageUrl,
            )
        }
    }

    fun increment(id: String) { viewModelScope.launch { listRepository.increment(id) } }

    fun decrement(id: String) { viewModelScope.launch { listRepository.decrement(id) } }

    fun toggle(id: String) { viewModelScope.launch { listRepository.toggle(id) } }

    fun delete(id: String) { viewModelScope.launch { listRepository.delete(id) } }
}
