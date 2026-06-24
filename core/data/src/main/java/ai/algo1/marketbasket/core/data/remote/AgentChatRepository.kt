package ai.algo1.marketbasket.core.data.remote

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI agent chat.
 * Stub — fully implemented in Task 2/Task 4.
 */
interface AgentChatRepository {
    // TODO(Task 4): Define streaming SSE chat methods
    fun streamChat(userMessage: String): Flow<String>
}
