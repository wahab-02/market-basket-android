package ai.algo1.marketbasket.core.domain.model

/**
 * Mirrors the TS GroceryItem (src/types/index.ts). Optional TS fields are nullable here.
 * NOTE: Supabase returns snake_case JSON (image_url, added_by_name); the data layer must map
 * those with @SerialName (or a snake_case naming strategy) when (de)serializing.
 */
data class GroceryItem(
    val id: String,
    val name: String,
    val categoryId: String,
    val checked: Boolean = false,
    val quantity: Int? = null,
    val source: String? = null,
    val imageUrl: String? = null,
    val addedByName: String? = null,
)

/** Mirrors the TS Category. `color`/`colorDark` are hex strings, as in the source data. */
data class Category(
    val id: String,
    val name: String,
    val color: String,
    val colorDark: String,
    val items: List<GroceryItem> = emptyList(),
)

/**
 * Mirrors the TS Promotion (Supabase row). Nullable fields match the TS `| null` columns.
 * NOTE: Supabase columns are snake_case (product_name, is_active, ...); the data layer must
 * map them with @SerialName when deserializing.
 */
data class Promotion(
    val id: String,
    val productName: String,
    val productImage: String?,
    val promotionText: String?,
    val saveAmount: String?,
    val originalPrice: Double?,
    val currentPrice: Double?,
    val category: String?,
    val barcode: String?,
    val badgeText: String?,
    val isActive: Boolean,
    val createdAt: String,
)
