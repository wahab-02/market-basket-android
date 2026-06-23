package ai.algo1.marketbasket.core.data.repository

import ai.algo1.marketbasket.core.data.BuildConfig
import ai.algo1.marketbasket.core.data.remote.ProductCategoriesResponse
import ai.algo1.marketbasket.core.data.remote.StoreProductDto
import ai.algo1.marketbasket.core.data.remote.StoreProductsResponse
import ai.algo1.marketbasket.core.domain.catalog.CategoryResolver
import ai.algo1.marketbasket.core.domain.model.CatalogProduct
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val json: Json,
) : CatalogRepository {

    private val baseUrl = BuildConfig.CATALOG_BASE_URL.trimEnd('/')
    private val apiKey = BuildConfig.CATALOG_API_KEY
    private val deviceId = BuildConfig.CATALOG_DEVICE_ID
    private val storeUrn = BuildConfig.CATALOG_STORE_URN
    private val partnerUrn = BuildConfig.CATALOG_PARTNER_URN

    override suspend fun searchProducts(term: String): List<CatalogProduct> {
        val trimmed = term.trim().ifEmpty { return emptyList() }
        return try {
            val response: StoreProductsResponse = client.get("$baseUrl/v1/stores/$storeUrn/products") {
                header("x-api-key", apiKey)
                header("x-device-id", deviceId)
                parameter("search", trimmed)
                parameter("size", "25")
                parameter("versioned", "false")
            }.body()
            val data = response.data ?: return emptyList()
            val seen = mutableSetOf<String>()
            data.mapNotNull { dto ->
                val key = (dto.displayName ?: dto.name ?: "").lowercase().trim()
                if (key.isEmpty() || !seen.add(key)) null else mapToProduct(dto)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun searchCategoryImage(term: String): String? {
        val trimmed = term.trim().ifEmpty { return null }
        return try {
            val encodedPartner = URLEncoder.encode(partnerUrn, "UTF-8")
            val response: ProductCategoriesResponse = client.get("$baseUrl/v1/partners/$encodedPartner/productcategories") {
                header("x-api-key", apiKey)
                header("x-device-id", deviceId)
                parameter("search", trimmed)
                parameter("size", "5")
                parameter("versioned", "false")
            }.body()
            val data = response.data ?: return null
            var bestScore = 0.0
            var bestImage: String? = null
            for (cat in data) {
                if (cat.isDeleted) continue
                val name = cat.name ?: continue
                val score = similarity(trimmed, name)
                if (score <= bestScore || score < SIMILARITY_THRESHOLD) continue
                val image = cat.media?.find {
                    !it.isDeleted && it.mediaType == "IMAGE" && !it.mediaLocation.isNullOrEmpty()
                }?.mediaLocation
                if (image != null) { bestScore = score; bestImage = image }
            }
            bestImage
        } catch (_: Exception) {
            null
        }
    }

    private fun mapToProduct(dto: StoreProductDto): CatalogProduct {
        val image = dto.media?.find { m ->
            !m.isDeleted && m.mediaType != "VIDEO" && m.tags?.isApproved != false && !m.mediaLocation.isNullOrEmpty()
        }?.mediaLocation
        return CatalogProduct(
            urn = dto.sku ?: dto.urn ?: "",
            name = dto.displayName ?: dto.name ?: "",
            imageUrl = image,
            category = resolveCategory(dto),
        )
    }

    private fun resolveCategory(dto: StoreProductDto): String {
        val attrs = dto.attrs
        attrs?.zone?.let { CategoryResolver.matchToKnown(it) }?.let { return it }
        attrs?.breadcrumbs?.forEach { crumb ->
            crumb.label?.let { CategoryResolver.matchToKnown(it) }?.let { return it }
        }
        attrs?.sourceCategoryPath?.let { path ->
            try {
                json.decodeFromString<List<String>>(path).forEach { segment ->
                    CategoryResolver.matchToKnown(segment)?.let { return it }
                }
            } catch (_: Exception) {}
        }
        attrs?.categories?.forEach { cat ->
            cat.name?.let { CategoryResolver.matchToKnown(it) }?.let { return it }
        }
        attrs?.hfssRestriction?.firstOrNull()?.hfssCategory
            ?.let { CategoryResolver.matchToKnown(it) }?.let { return it }
        val raw = dto.category ?: dto.parentHierarchy?.lastOrNull()?.name
        raw?.let { CategoryResolver.matchToKnown(it) }?.let { return it }
        return CategoryResolver.resolveCategoryFromText(raw, dto.displayName ?: dto.name ?: "")
    }

    private fun similarity(search: String, categoryName: String): Double {
        val a = search.lowercase().trim()
        val b = categoryName.lowercase().trim()
        if (a == b) return 1.0
        if (b.contains(a) || a.contains(b)) return 0.85
        val aWords = a.split(Regex("\\s+")).filter { it.isNotEmpty() }.toSet()
        val bWords = b.split(Regex("\\s+")).filter { it.isNotEmpty() }.toSet()
        val intersection = aWords.intersect(bWords).size
        val union = (aWords + bWords).size
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }

    companion object {
        private const val SIMILARITY_THRESHOLD = 0.5
    }
}
