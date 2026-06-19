package ai.algo1.marketbasket.core.data.repository

import ai.algo1.marketbasket.core.data.remote.ListChange
import ai.algo1.marketbasket.core.data.remote.RemoteListDataSource
import ai.algo1.marketbasket.core.domain.list.ListReducer
import ai.algo1.marketbasket.core.domain.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListRepository @Inject constructor(private val remote: RemoteListDataSource) {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private var userId: String? = null

    suspend fun load(publicId: String) {
        val user = remote.loadUser(publicId) ?: return
        userId = user.id
        // Group items by category id into buckets; the UI layer (Plan 07) applies seeded order/colors.
        val items = remote.loadItems(user.id)
        _categories.value = items.groupBy { it.categoryId }.map { (cid, list) ->
            Category(id = cid, name = cid, color = "", colorDark = "", items = list)
        }
    }

    suspend fun addOrIncrement(
        name: String,
        categoryId: String,
        source: String?,
        imageUrl: String? = null,
        quantity: Int = 1,
        moveExistingToCategory: Boolean = false,
    ) {
        val uid = userId ?: return
        val result = ListReducer.addOrIncrement(
            _categories.value, name, categoryId, source, imageUrl, quantity, moveExistingToCategory,
            newId = UUID.randomUUID().toString(),
        )
        _categories.value = result.categories
        if (result.inserted) remote.insertItem(uid, result.item) else remote.updateItem(result.item.id, result.item)
    }

    suspend fun toggle(itemId: String) {
        val r = ListReducer.toggle(_categories.value, itemId)
        _categories.value = r.categories
        r.newChecked?.let { remote.setChecked(itemId, it) }
    }

    suspend fun delete(itemId: String) {
        _categories.value = ListReducer.delete(_categories.value, itemId)
        remote.deleteItem(itemId)
    }

    fun observeRealtime(scope: CoroutineScope) {
        val uid = userId ?: return
        remote.changes(uid).onEach { change ->
            _categories.value = when (change) {
                is ListChange.Upserted ->
                    if (change.isUpdate) ListReducer.applyRealtimeUpdate(_categories.value, change.item)
                    else ListReducer.applyRealtimeInsert(_categories.value, change.item)
                is ListChange.Deleted -> ListReducer.applyRealtimeDelete(_categories.value, change.id)
            }
        }.launchIn(scope)
    }
}
