package ai.algo1.marketbasket.core.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network implementation of [AgentChatRepository].
 * Stub — fully implemented in Task 4.
 */
@Singleton
class AgentChatService @Inject constructor() : AgentChatRepository {
    // TODO(Task 4): Inject @StreamingHttpClient HttpClient and implement SSE streaming
    override fun streamChat(threadId: String, publicId: String, message: String): Flow<AgentEvent> = flow {
        TODO("Implemented in Task 4")
    }

    override suspend fun fetchThreadMessages(threadId: String, publicId: String): List<MessageRow> {
        TODO("Implemented in Task 4")
    }
}
