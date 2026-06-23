package ai.algo1.marketbasket.core.data.remote

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow

sealed interface ListChange {
    data class Upserted(val item: GroceryItem, val isUpdate: Boolean) : ListChange
    data class Deleted(val id: String) : ListChange
}

interface RemoteListDataSource {
    suspend fun loadUser(publicId: String): AppUserDto?
    suspend fun loadItems(userId: String): List<GroceryItem>
    suspend fun insertItem(userId: String, item: GroceryItem)
    suspend fun updateItem(id: String, item: GroceryItem)
    suspend fun setChecked(id: String, checked: Boolean)
    suspend fun setImageUrl(id: String, imageUrl: String)
    suspend fun deleteItem(id: String)
    fun changes(userId: String): Flow<ListChange>
}
