package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.remote.RawAgentEvent
import ai.algo1.marketbasket.core.data.remote.AgentEvent
import ai.algo1.marketbasket.core.data.remote.toAgentEvent
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentChatModelsTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test fun `parses token event`() {
        val raw = """{"type":"token","text":"Hello"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.Token)
        assertEquals("Hello", (event as AgentEvent.Token).text)
    }

    @Test fun `parses tool_pending event`() {
        val raw = """{"type":"tool_pending","tool":"search","label":"Searching"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.ToolPending)
        assertEquals("search", (event as AgentEvent.ToolPending).tool)
        assertEquals("Searching", event.label)
    }

    @Test fun `parses tool_start event`() {
        val raw = """{"type":"tool_start","tool":"search","label":"Searching","input_summary":"pasta"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.ToolStart)
        assertEquals("pasta", (event as AgentEvent.ToolStart).inputSummary)
    }

    @Test fun `parses tool_end success event`() {
        val raw = """{"type":"tool_end","tool":"search","success":true,"output_summary":"Found 3"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.ToolEnd)
        assertTrue((event as AgentEvent.ToolEnd).success)
        assertEquals("Found 3", event.outputSummary)
    }

    @Test fun `parses final event with suggestions`() {
        val raw = """{"type":"final","response_text":"Done!","suggestions":["Next step","Try this"]}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.Final)
        assertEquals("Done!", (event as AgentEvent.Final).responseText)
        assertEquals(listOf("Next step", "Try this"), event.suggestions)
    }

    @Test fun `parses error event`() {
        val raw = """{"type":"error","message":"Oops"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.Error)
        assertEquals("Oops", (event as AgentEvent.Error).message)
    }

    @Test fun `unknown type becomes Unknown`() {
        val raw = """{"type":"future_event","data":"x"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.Unknown)
    }
}
