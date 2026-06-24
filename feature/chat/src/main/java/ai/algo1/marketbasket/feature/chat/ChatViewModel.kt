package ai.algo1.marketbasket.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.local.ChatThreadRepository
import ai.algo1.marketbasket.core.data.remote.AgentChatRepository
import ai.algo1.marketbasket.core.data.remote.AgentEvent
import ai.algo1.marketbasket.core.data.remote.RecipeDataDto
import ai.algo1.marketbasket.core.data.repository.PublicIdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val publicIdProvider: PublicIdProvider,
    private val agentChatRepository: AgentChatRepository,
    private val chatThreadStore: ChatThreadRepository,
    private val json: Json,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _liveSteps = MutableStateFlow<List<LiveStep>>(emptyList())
    val liveSteps: StateFlow<List<LiveStep>> = _liveSteps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _addedRecipeIds = MutableStateFlow<Set<String>>(emptySet())
    val addedRecipeIds: StateFlow<Set<String>> = _addedRecipeIds.asStateFlow()

    private var activeJob: Job? = null

    // Cached thread state — updated whenever publicId changes.
    private var cachedPublicId: String? = null
    private var cachedThreadId: String? = null

    init {
        viewModelScope.launch {
            publicIdProvider.publicId.filterNotNull().collect { publicId ->
                if (publicId != cachedPublicId) {
                    cachedPublicId = publicId
                    cachedThreadId = chatThreadStore.getOrCreateThreadId(publicId)
                    hydrateMessages()
                }
            }
        }
    }

    private suspend fun hydrateMessages() {
        val pid = cachedPublicId ?: return
        val tid = cachedThreadId ?: return
        val rows = agentChatRepository.fetchThreadMessages(tid, pid)
        _messages.value = rows.mapNotNull { row ->
            when (row.role) {
                "user" -> ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = Role.User,
                    text = row.content,
                    artifacts = emptyList(),
                    steps = emptyList(),
                    suggestions = emptyList(),
                )
                "assistant" -> ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = Role.Assistant,
                    text = row.content,
                    artifacts = row.artifact?.let { parseArtifact("recipe", it) }
                        ?.let { listOf(it) } ?: emptyList(),
                    steps = emptyList(),
                    suggestions = emptyList(),
                )
                else -> null
            }
        }
    }

    /**
     * Resolves the publicId and threadId, waiting for the first non-null publicId if needed.
     * Returns a Pair<publicId, threadId> or null if already disposed.
     */
    private suspend fun resolveIds(): Pair<String, String>? {
        // Use cached value if available to avoid redundant suspending calls.
        val pid = cachedPublicId ?: publicIdProvider.publicId.filterNotNull().first()
        val tid = cachedThreadId ?: run {
            val id = chatThreadStore.getOrCreateThreadId(pid)
            cachedPublicId = pid
            cachedThreadId = id
            id
        }
        return pid to tid
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        activeJob?.cancel()

        // Append the user message optimistically before the coroutine resolves IDs.
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.User,
            text = text,
            artifacts = emptyList(),
            steps = emptyList(),
            suggestions = emptyList(),
        )
        _messages.value = _messages.value + userMsg
        _isLoading.value = true
        _streamingText.value = ""
        _liveSteps.value = emptyList()

        activeJob = viewModelScope.launch {
            val ids = resolveIds()
            if (ids == null) {
                _isLoading.value = false
                return@launch
            }
            val (pid, tid) = ids

            val stepsAcc = mutableListOf<LiveStep>()
            val artifactsAcc = mutableListOf<Artifact>()
            var textAcc = ""

            try {
                agentChatRepository.streamChat(tid, pid, text).collect { event ->
                    when (event) {
                        is AgentEvent.Token -> {
                            textAcc += event.text
                            _streamingText.value = textAcc
                        }
                        is AgentEvent.ToolPending -> {
                            val alreadyTracked = stepsAcc.any {
                                it.tool == event.tool &&
                                    it.status != StepStatus.Done &&
                                    it.status != StepStatus.Error
                            }
                            if (!alreadyTracked) {
                                stepsAcc.add(
                                    LiveStep(
                                        tool = event.tool,
                                        label = event.label,
                                        status = StepStatus.Pending,
                                    )
                                )
                                _liveSteps.value = stepsAcc.toList()
                            }
                        }
                        is AgentEvent.ToolStart -> {
                            val idx = stepsAcc.indexOfLast {
                                it.tool == event.tool &&
                                    (it.status == StepStatus.Pending || it.status == StepStatus.Running)
                            }
                            if (idx >= 0) {
                                stepsAcc[idx] = stepsAcc[idx].copy(
                                    status = StepStatus.Running,
                                    inputSummary = event.inputSummary,
                                )
                            } else {
                                stepsAcc.add(
                                    LiveStep(
                                        tool = event.tool,
                                        label = event.label,
                                        status = StepStatus.Running,
                                        inputSummary = event.inputSummary,
                                    )
                                )
                            }
                            _liveSteps.value = stepsAcc.toList()
                        }
                        is AgentEvent.ToolEnd -> {
                            val idx = stepsAcc.indexOfLast {
                                it.tool == event.tool &&
                                    (it.status == StepStatus.Running || it.status == StepStatus.Pending)
                            }
                            if (idx >= 0) {
                                stepsAcc[idx] = stepsAcc[idx].copy(
                                    status = if (event.success) StepStatus.Done else StepStatus.Error,
                                    outputSummary = event.outputSummary,
                                )
                                _liveSteps.value = stepsAcc.toList()
                            }
                        }
                        is AgentEvent.ArtifactEvent -> {
                            parseArtifact(event.kind, event.data)?.let { artifactsAcc.add(it) }
                        }
                        is AgentEvent.Final -> {
                            val finalText =
                                event.responseText?.takeIf { it.isNotEmpty() } ?: textAcc
                            val assistantMsg = ChatMessage(
                                id = UUID.randomUUID().toString(),
                                role = Role.Assistant,
                                text = finalText,
                                artifacts = artifactsAcc.toList(),
                                steps = stepsAcc.toList(),
                                suggestions = event.suggestions ?: emptyList(),
                            )
                            _messages.value = _messages.value + assistantMsg
                        }
                        is AgentEvent.Error -> {
                            val errMsg = ChatMessage(
                                id = UUID.randomUUID().toString(),
                                role = Role.Assistant,
                                text = event.message
                                    ?: "Something went wrong. Please try again.",
                                artifacts = emptyList(),
                                steps = stepsAcc.toList(),
                                suggestions = emptyList(),
                            )
                            _messages.value = _messages.value + errMsg
                        }
                        else -> { /* Start, Unknown — ignore */ }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                val networkErr = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = Role.Assistant,
                    text = "Network error — please check your connection and try again.",
                    artifacts = emptyList(),
                    steps = emptyList(),
                    suggestions = emptyList(),
                )
                _messages.value = _messages.value + networkErr
            } finally {
                _streamingText.value = ""
                _liveSteps.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun abort() {
        activeJob?.cancel()
        activeJob = null
        _isLoading.value = false
        _streamingText.value = ""
        _liveSteps.value = emptyList()
    }

    fun handleAddIngredients(messageId: String, ingredients: List<String>) {
        _addedRecipeIds.value = _addedRecipeIds.value + messageId
        sendMessage("Add these ingredients to my list: ${ingredients.joinToString(", ")}")
    }

    private fun parseArtifact(kind: String, data: JsonElement): Artifact? {
        if (kind != "recipe" || data == JsonNull) return null
        return try {
            val dto = json.decodeFromJsonElement<RecipeDataDto>(data)
            Artifact.Recipe(
                RecipeData(
                    title = dto.title,
                    cuisine = dto.cuisine,
                    servings = dto.servings,
                    prepTimeMinutes = dto.prepTimeMinutes,
                    cookTimeMinutes = dto.cookTimeMinutes,
                    ingredients = dto.ingredients.map { RecipeIngredient(it.name, it.quantity) },
                    steps = dto.steps,
                    tags = dto.tags,
                )
            )
        } catch (_: Exception) {
            null
        }
    }
}
