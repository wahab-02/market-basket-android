package ai.algo1.marketbasket.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItemDto(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val quantity: Int? = null,
    val category: String,
    val checked: Boolean = false,
    val source: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
