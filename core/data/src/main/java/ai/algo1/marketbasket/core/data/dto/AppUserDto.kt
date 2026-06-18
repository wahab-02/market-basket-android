package ai.algo1.marketbasket.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUserDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("slack_id") val slackId: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("list_code") val listCode: String? = null,
    @SerialName("alexa_connected") val alexaConnected: Boolean = false,
    @SerialName("chatgpt_connected") val chatgptConnected: Boolean = false,
    @SerialName("claude_connected") val claudeConnected: Boolean = false,
    @SerialName("google_connected") val googleConnected: Boolean = false,
    @SerialName("siri_connected") val siriConnected: Boolean = false,
)
