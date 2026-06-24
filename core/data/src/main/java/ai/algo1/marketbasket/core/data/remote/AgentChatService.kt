package ai.algo1.marketbasket.core.data.remote

import ai.algo1.marketbasket.core.data.BuildConfig
import ai.algo1.marketbasket.core.data.di.StreamingHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentChatService @Inject constructor(
    @StreamingHttpClient private val streamingClient: HttpClient,
    private val httpClient: HttpClient,
    private val json: Json,
) : AgentChatRepository {

    private val base = BuildConfig.BACKEND_BASE_URL.trimEnd('/')

    override fun streamChat(
        threadId: String,
        publicId: String,
        message: String,
    ): Flow<AgentEvent> = channelFlow {
        streamingClient.preparePost("$base/api/agent/chat/stream") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, "text/event-stream")
            setBody(StreamChatRequest(threadId = threadId, publicId = publicId, message = message))
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                val trimmed = line.trimStart()
                if (!trimmed.startsWith("data:")) continue
                val raw = trimmed.removePrefix("data:").trim()
                if (raw == "[DONE]") break
                if (raw.isEmpty()) continue
                try {
                    val rawEvent = json.decodeFromString<RawAgentEvent>(raw)
                    send(rawEvent.toAgentEvent())
                } catch (_: Exception) { /* skip malformed line */ }
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun fetchThreadMessages(
        threadId: String,
        publicId: String,
    ): List<MessageRow> = try {
        httpClient.get("$base/api/agent/threads/$threadId") {
            parameter("public_id", publicId)
        }.body<ThreadMessagesResponse>().messages
    } catch (_: Exception) {
        emptyList()
    }
}
