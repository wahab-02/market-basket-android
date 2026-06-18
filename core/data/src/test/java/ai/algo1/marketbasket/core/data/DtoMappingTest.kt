package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.dto.ShoppingListItemDto
import ai.algo1.marketbasket.core.data.dto.toDomain
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
