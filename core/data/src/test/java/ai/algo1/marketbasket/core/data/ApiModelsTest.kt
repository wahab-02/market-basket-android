package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.remote.EnsureListCodeRequest
import ai.algo1.marketbasket.core.data.remote.EnsureListCodeResponse
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
}
