package ai.algo1.marketbasket.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AssistantBubbleBg = Color(0xFFF8F9FB)

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    streamingText: String,
    liveSteps: List<LiveStep>,
    isLoading: Boolean,
    onAddIngredients: (messageId: String, ingredients: List<String>) -> Unit,
    addedRecipeIds: Set<String>,
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when messages or streaming text change
    val itemCount = messages.size + (if (isLoading) 1 else 0)
    LaunchedEffect(messages.size, streamingText, liveSteps.size) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
    ) {
        itemsIndexed(messages, key = { _, msg -> msg.id }) { _, message ->
            MessageBubble(
                message = message,
                onAddIngredients = onAddIngredients,
                addedRecipeIds = addedRecipeIds,
                onSuggestion = onSuggestion,
            )
        }

        if (isLoading) {
            item(key = "loading") {
                val showTyping = streamingText.isEmpty() && liveSteps.isEmpty()
                val showStreaming = streamingText.isNotEmpty() || liveSteps.isNotEmpty()

                if (showTyping) {
                    Box(
                        modifier = Modifier
                            .background(AssistantBubbleBg, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) { TypingDots() }
                }

                if (showStreaming) {
                    val streamingMsg = ChatMessage(
                        id = "streaming",
                        role = Role.Assistant,
                        text = streamingText,
                        artifacts = emptyList(),
                        steps = emptyList(),
                        suggestions = emptyList(),
                    )
                    MessageBubble(
                        message = streamingMsg,
                        streamingText = streamingText,
                        isStreaming = true,
                        liveSteps = liveSteps,
                        onAddIngredients = onAddIngredients,
                        addedRecipeIds = addedRecipeIds,
                        onSuggestion = onSuggestion,
                    )
                }
            }
        }
    }
}
