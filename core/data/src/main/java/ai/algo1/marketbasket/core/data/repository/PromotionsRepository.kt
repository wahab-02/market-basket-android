package ai.algo1.marketbasket.core.data.repository

import ai.algo1.marketbasket.core.data.dto.PromotionDto
import ai.algo1.marketbasket.core.data.dto.toDomain
import ai.algo1.marketbasket.core.domain.model.Promotion
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromotionsRepository @Inject constructor(private val supabase: SupabaseClient) {
    suspend fun activePromotions(): List<Promotion> =
        supabase.from("promotions")
            .select { filter { eq("is_active", true) } }
            .decodeList<PromotionDto>()
            .map { it.toDomain() }
}
