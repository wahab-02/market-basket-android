package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.remote.EnsureListCodeRequest
import ai.algo1.marketbasket.core.data.remote.EnsureListCodeResponse
import ai.algo1.marketbasket.core.data.remote.ImportItem
import ai.algo1.marketbasket.core.data.remote.ImportRequest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiModelsTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test fun ensureListCodeRequestSerializesPublicIdSnakeCase() {
        val body = json.encodeToString(EnsureListCodeRequest.serializer(), EnsureListCodeRequest("abc123"))
        assertTrue(body.contains("\"public_id\":\"abc123\""))
    }

    @Test fun ensureListCodeResponseParsesListCode() {
        val resp = json.decodeFromString(EnsureListCodeResponse.serializer(), """{"list_code":"AB12CD"}""")
        assertEquals("AB12CD", resp.listCode)
    }

    @Test fun importRequestSerializesSnakeCaseFields() {
        val req = ImportRequest(
            publicId = "u1",
            items = listOf(ImportItem(name = "Milk", quantity = 2, category = "dairy", imageUrl = "http://img")),
        )
        val body = json.encodeToString(ImportRequest.serializer(), req)
        assertTrue(body.contains("\"public_id\":\"u1\""))
        assertTrue(body.contains("\"image_url\":\"http://img\""))
    }
}
