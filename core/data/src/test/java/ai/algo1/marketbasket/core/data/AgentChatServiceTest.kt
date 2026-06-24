package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.remote.AgentEvent
import ai.algo1.marketbasket.core.data.remote.RawAgentEvent
import ai.algo1.marketbasket.core.data.remote.toAgentEvent
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Unit-tests the SSE line parsing logic in isolation — no HTTP client needed.
class AgentChatServiceTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private fun parseLine(line: String): AgentEvent? {
        val trimmed = line.trimStart()
        if (!trimmed.startsWith("data:")) return null
        val raw = trimmed.removePrefix("data:").trim()
        if (raw == "[DONE]" || raw.isEmpty()) return null
        return try { json.decodeFromString<RawAgentEvent>(raw).toAgentEvent() } catch (_: Exception) { null }
    }

    @Test fun `parses token line`() {
        val event = parseLine("""data: {"type":"token","text":"Hi"}""")
        assertTrue(event is AgentEvent.Token)
        assertEquals("Hi", (event as AgentEvent.Token).text)
    }

    @Test fun `ignores non-data lines`() {
        val event = parseLine("event: message")
        assertEquals(null, event)
    }

    @Test fun `returns null for DONE sentinel`() {
        val event = parseLine("data: [DONE]")
        assertEquals(null, event)
    }

    @Test fun `returns null for malformed json`() {
        val event = parseLine("data: {broken}")
        assertEquals(null, event)
    }

    @Test fun `parses data line with leading whitespace`() {
        val event = parseLine("  data: {\"type\":\"token\",\"text\":\"x\"}")
        assertTrue(event is AgentEvent.Token)
    }
}
