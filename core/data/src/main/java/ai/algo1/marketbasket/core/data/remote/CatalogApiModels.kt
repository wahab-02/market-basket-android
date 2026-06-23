package ai.algo1.marketbasket.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreProductsResponse(val data: List<StoreProductDto>? = null)

@Serializable
data class StoreProductDto(
    val sku: String? = null,
    val urn: String? = null,
    val name: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val category: String? = null,
    @SerialName("parent_hierarchy") val parentHierarchy: List<HierarchyItemDto>? = null,
    val ingredients: String? = null,
    val attrs: ProductAttrsDto? = null,
    val media: List<ProductMediaDto>? = null,
    val stores: List<ProductStoreDto>? = null,
)

@Serializable
data class HierarchyItemDto(val urn: String? = null, val name: String? = null)

@Serializable
data class ProductAttrsDto(
    val zone: String? = null,
    val categories: List<AttrCategoryDto>? = null,
    val breadcrumbs: List<AttrBreadcrumbDto>? = null,
    @SerialName("_source_category_path") val sourceCategoryPath: String? = null,
    @SerialName("hfss_restriction") val hfssRestriction: List<HfssRestrictionDto>? = null,
    @SerialName("dietary information") val dietaryInformation: String? = null,
)

@Serializable
data class AttrCategoryDto(val id: String? = null, val name: String? = null)

@Serializable
data class AttrBreadcrumbDto(val url: String? = null, val label: String? = null)

@Serializable
data class HfssRestrictionDto(@SerialName("hfss_category") val hfssCategory: String? = null)

@Serializable
data class ProductMediaDto(
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("media_location") val mediaLocation: String? = null,
    val tags: ProductMediaTagsDto? = null,
)

@Serializable
data class ProductMediaTagsDto(
    @SerialName("is_approved") val isApproved: Boolean? = null,
)

@Serializable
data class ProductStoreDto(
    @SerialName("store_urn") val storeUrn: String? = null,
    val price: Double? = null,
    val units: String? = null,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)

@Serializable
data class ProductCategoriesResponse(val data: List<ProductCategoryDto>? = null)

@Serializable
data class ProductCategoryDto(
    val name: String? = null,
    val media: List<CategoryMediaDto>? = null,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)

@Serializable
data class CategoryMediaDto(
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("media_location") val mediaLocation: String? = null,
)
