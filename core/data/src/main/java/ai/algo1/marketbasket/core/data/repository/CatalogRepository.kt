package ai.algo1.marketbasket.core.data.repository

import ai.algo1.marketbasket.core.domain.model.CatalogProduct

interface CatalogRepository {
    suspend fun searchProducts(term: String): List<CatalogProduct>
    suspend fun searchCategoryImage(term: String): String?
}
