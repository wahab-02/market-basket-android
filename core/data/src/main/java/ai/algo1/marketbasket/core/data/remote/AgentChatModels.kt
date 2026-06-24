package ai.algo1.marketbasket.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── Wire DTO ─────────────────────────────────────────────────────────────────

@Serializable
data class RawAgentEvent(
    val type: String,
    val text: String? = null,
    val tool: String? = null,
    val label: String? = null,
    @SerialName("input_summary") val inputSummary: String? = null,
    @SerialName("output_summary") val outputSummary: String? = null,
    val success: Boolean? = null,
    val kind: String? = null,
    val data: JsonElement? = null,
    @SerialName("response_text") val responseText: String? = null,
    val suggestions: List<String>? = null,
    val message: String? = null,
)

// ── Domain sealed class ───────────────────────────────────────────────────────

sealed class AgentEvent {
    data object Start : AgentEvent()
    data class Token(val text: String) : AgentEvent()
    data class ToolPending(val tool: String, val label: String) : AgentEvent()
    data class ToolStart(val tool: String, val label: String, val inputSummary: String?) : AgentEvent()
    data class ToolEnd(val tool: String, val success: Boolean, val outputSummary: String?) : AgentEvent()
    data class ArtifactEvent(val kind: String, val data: JsonElement) : AgentEvent()
    data class Final(val responseText: String?, val suggestions: List<String>?) : AgentEvent()
    data class Error(val message: String?) : AgentEvent()
    data class Unknown(val type: String) : AgentEvent()
}

fun RawAgentEvent.toAgentEvent(): AgentEvent = when (type) {
    "start"        -> AgentEvent.Start
    "token"        -> AgentEvent.Token(text ?: "")
    "tool_pending" -> AgentEvent.ToolPending(tool ?: "", label ?: "")
    "tool_start"   -> AgentEvent.ToolStart(tool ?: "", label ?: "", inputSummary)
    "tool_end"     -> AgentEvent.ToolEnd(tool ?: "", success ?: false, outputSummary)
    "artifact"     -> AgentEvent.ArtifactEvent(kind ?: "", data ?: kotlinx.serialization.json.JsonNull)
    "final"        -> AgentEvent.Final(responseText, suggestions)
    "error"        -> AgentEvent.Error(message)
    else           -> AgentEvent.Unknown(type)
}

// ── Thread hydration DTOs ─────────────────────────────────────────────────────

@Serializable
data class MessageRow(
    val role: String,
    val content: String,
    val artifact: JsonElement? = null,
)

@Serializable
data class ThreadMessagesResponse(
    val messages: List<MessageRow> = emptyList(),
)

@Serializable
data class StreamChatRequest(
    @SerialName("thread_id") val threadId: String,
    @SerialName("public_id") val publicId: String,
    val message: String,
)

// ── Recipe artifact DTO (for JSON decoding in ChatViewModel) ──────────────────

@Serializable
data class RecipeDataDto(
    val title: String,
    val cuisine: String,
    val servings: Int,
    @SerialName("prep_time_minutes") val prepTimeMinutes: Int,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int,
    val ingredients: List<RecipeIngredientDto>,
    val steps: List<String>,
    val tags: List<String>,
)

@Serializable
data class RecipeIngredientDto(val name: String, val quantity: String)
