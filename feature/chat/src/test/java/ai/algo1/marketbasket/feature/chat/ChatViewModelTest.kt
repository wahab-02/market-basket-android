package ai.algo1.marketbasket.feature.chat

import ai.algo1.marketbasket.core.data.local.ChatThreadRepository
import ai.algo1.marketbasket.core.data.remote.AgentChatRepository
import ai.algo1.marketbasket.core.data.remote.AgentEvent
import ai.algo1.marketbasket.core.data.remote.MessageRow
import ai.algo1.marketbasket.core.data.repository.PublicIdProvider
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    // Fake PublicIdProvider — trivially implements the single-method interface
    private class FakePublicIdProvider(id: String?) : PublicIdProvider {
        override val publicId: StateFlow<String?> = MutableStateFlow(id)
    }

    // Fake ChatThreadRepository — just an interface, easy to implement
    private val fakeChatThreadRepo = object : ChatThreadRepository {
        override suspend fun getOrCreateThreadId(publicId: String) = "test-thread-id"
    }

    private fun makeRepo(vararg events: AgentEvent): AgentChatRepository = object : AgentChatRepository {
        override fun streamChat(t: String, p: String, m: String): Flow<AgentEvent> = flowOf(*events)
        override suspend fun fetchThreadMessages(t: String, p: String): List<MessageRow> = emptyList()
    }

    private fun viewModel(repo: AgentChatRepository): ChatViewModel = ChatViewModel(
        publicIdProvider = FakePublicIdProvider("pub-123"),
        agentChatRepository = repo,
        chatThreadStore = fakeChatThreadRepo,
        json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; explicitNulls = false },
    )

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `sendMessage appends user message immediately`() = runTest {
        val vm = viewModel(makeRepo(AgentEvent.Final(responseText = "OK", suggestions = null)))
        vm.messages.test {
            awaitItem() // initial empty list
            vm.sendMessage("hello")
            val afterSend = awaitItem()
            assertTrue(afterSend.any { it.role == Role.User && it.text == "hello" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `token events accumulate in streamingText`() = runTest {
        val repo = makeRepo(
            AgentEvent.Token("Hel"),
            AgentEvent.Token("lo"),
            AgentEvent.Final(responseText = "Hello", suggestions = null),
        )
        val vm = viewModel(repo)
        vm.sendMessage("hi")
        testDispatcher.scheduler.advanceUntilIdle()
        // after final, streamingText clears
        assertEquals("", vm.streamingText.value)
        // assistant message added
        assertTrue(vm.messages.value.any { it.role == Role.Assistant && it.text == "Hello" })
    }

    @Test fun `abort clears loading state`() = runTest {
        val repo = object : AgentChatRepository {
            override fun streamChat(t: String, p: String, m: String): Flow<AgentEvent> =
                kotlinx.coroutines.flow.flow { kotlinx.coroutines.delay(10_000) }
            override suspend fun fetchThreadMessages(t: String, p: String) = emptyList<MessageRow>()
        }
        val vm = viewModel(repo)
        vm.sendMessage("hi")
        testDispatcher.scheduler.advanceTimeBy(100)
        vm.abort()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.isLoading.value)
        assertEquals("", vm.streamingText.value)
    }

    @Test fun `error event appends assistant error message`() = runTest {
        val vm = viewModel(makeRepo(AgentEvent.Error("Something went wrong. Please try again.")))
        vm.sendMessage("hi")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.messages.value.any { it.role == Role.Assistant && it.text.contains("Something went wrong") })
        assertFalse(vm.isLoading.value)
    }

    @Test fun `hydrateMessages maps user and assistant rows, skips unknown roles`() = runTest {
        val rows = listOf(
            MessageRow(role = "user", content = "what's for dinner?"),
            MessageRow(role = "assistant", content = "How about pasta?"),
            MessageRow(role = "unknown", content = "ignored"),
        )
        val repo = object : AgentChatRepository {
            override fun streamChat(t: String, p: String, m: String): Flow<AgentEvent> = flowOf()
            override suspend fun fetchThreadMessages(t: String, p: String): List<MessageRow> = rows
        }
        val vm = viewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()
        val msgs = vm.messages.value
        assertEquals(2, msgs.size)
        assertEquals(Role.User, msgs[0].role)
        assertEquals("what's for dinner?", msgs[0].text)
        assertEquals(Role.Assistant, msgs[1].role)
        assertEquals("How about pasta?", msgs[1].text)
    }

    @Test fun `handleAddIngredients sends formatted follow-up and marks recipe added`() = runTest {
        var sentMessage: String? = null
        val repo = object : AgentChatRepository {
            override fun streamChat(t: String, p: String, m: String): Flow<AgentEvent> {
                sentMessage = m
                return flowOf(AgentEvent.Final(responseText = "Done", suggestions = null))
            }
            override suspend fun fetchThreadMessages(t: String, p: String) = emptyList<MessageRow>()
        }
        val vm = viewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.handleAddIngredients("msg-1", listOf("2 eggs", "1 cup milk"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Add these ingredients to my list: 2 eggs, 1 cup milk", sentMessage)
        assertTrue(vm.addedRecipeIds.value.contains("msg-1"))
    }
}
