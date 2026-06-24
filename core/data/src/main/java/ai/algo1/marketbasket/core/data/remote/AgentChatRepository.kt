package ai.algo1.marketbasket.core.data.remote

import kotlinx.coroutines.flow.Flow

interface AgentChatRepository {
    fun streamChat(threadId: String, publicId: String, message: String): Flow<AgentEvent>
    suspend fun fetchThreadMessages(threadId: String, publicId: String): List<MessageRow>
}
