package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.data.dto.PromotionDto
import ai.algo1.marketbasket.core.data.dto.ShoppingListItemDto
import ai.algo1.marketbasket.core.data.dto.toDomain
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DtoMappingTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test fun deserializesSnakeCaseRowAndMapsCategoryToCategoryId() {
        val row = """
            {"id":"u1","user_id":"owner1","name":"Milk","quantity":2,"category":"dairy",
             "checked":false,"source":"voice","image_url":null,"created_at":"2025-01-01T00:00:00Z"}
        """.trimIndent()
        val dto = json.decodeFromString(ShoppingListItemDto.serializer(), row)
        assertEquals("dairy", dto.category)

        val item = dto.toDomain()
        assertEquals("u1", item.id)
        assertEquals("Milk", item.name)
        assertEquals("dairy", item.categoryId)   // column `category` -> domain `categoryId`
        assertEquals(2, item.quantity)
        assertFalse(item.checked)
        assertEquals("voice", item.source)
        assertEquals(null, item.imageUrl)
    }

    @Test fun toleratesMissingOptionalColumns() {
        val row = """{"id":"x","user_id":"o","name":"Bread","category":"bakery","checked":true}"""
        val dto = json.decodeFromString(ShoppingListItemDto.serializer(), row)
        val item = dto.toDomain()
        assertEquals("bakery", item.categoryId)
        assertEquals(null, item.quantity)
        assertEquals(null, item.imageUrl)
    }

    @Test fun appUserDeserializesPublicIdAndConnectionFlags() {
        val row = """{"id":"u1","public_id":"abc","display_name":"Sam","list_code":"AB12CD","google_connected":true}"""
        val dto = json.decodeFromString(AppUserDto.serializer(), row)
        assertEquals("abc", dto.publicId)
        assertEquals("Sam", dto.displayName)
        assertEquals("AB12CD", dto.listCode)
        assertTrue(dto.googleConnected)
        assertFalse(dto.alexaConnected)   // default when absent
    }

    @Test fun promotionDeserializesSnakeCaseAndMaps() {
        val row = """
            {"id":"p1","product_name":"Cookies","product_image":"http://img","promotion_text":"2 for 1",
             "save_amount":"2.00","original_price":4.0,"current_price":2.0,"category":"snacks",
             "barcode":"123","badge_text":"DEAL","is_active":true,"created_at":"2025-01-01T00:00:00Z"}
        """.trimIndent()
        val dto = json.decodeFromString(PromotionDto.serializer(), row)
        assertTrue(dto.isActive)

        val promo = dto.toDomain()
        assertEquals("p1", promo.id)
        assertEquals("Cookies", promo.productName)
        assertEquals("snacks", promo.category)
        assertEquals("DEAL", promo.badgeText)
        assertTrue(promo.isActive)
        assertEquals(2.0, promo.currentPrice!!, 1e-9)
    }
}
