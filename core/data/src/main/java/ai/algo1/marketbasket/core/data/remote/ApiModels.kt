package ai.algo1.marketbasket.core.data.remote

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.data.dto.ShoppingListItemDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnsureListCodeRequest(@SerialName("public_id") val publicId: String)

@Serializable
data class EnsureListCodeResponse(@SerialName("list_code") val listCode: String)

@Serializable
data class ImportItem(
    val name: String,
    val quantity: Int,
    val category: String,
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class ImportRequest(
    @SerialName("public_id") val publicId: String,
    val items: List<ImportItem>,
)

@Serializable
data class ImportResponse(
    val user: AppUserDto,
    val items: List<ShoppingListItemDto>,
)

@Serializable
data class GoogleSyncResponse(val synced: Int = 0, val error: String? = null)
