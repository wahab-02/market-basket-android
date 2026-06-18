package ai.algo1.marketbasket.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromotionDto(
    val id: String,
    @SerialName("product_name") val productName: String,
    @SerialName("product_image") val productImage: String? = null,
    @SerialName("promotion_text") val promotionText: String? = null,
    @SerialName("save_amount") val saveAmount: String? = null,
    @SerialName("original_price") val originalPrice: Double? = null,
    @SerialName("current_price") val currentPrice: Double? = null,
    val category: String? = null,
    val barcode: String? = null,
    @SerialName("badge_text") val badgeText: String? = null,
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
)
