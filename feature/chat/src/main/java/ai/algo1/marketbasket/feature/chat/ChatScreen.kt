package ai.algo1.marketbasket.feature.chat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val HeaderBg     = Color.White
private val DividerColor = Color(0xFFF0F0F4)
private val SubtitleColor = Color(0xFF9CA3AF)
private val BackButtonBg  = Color(0xFFF3F4F6)

private val SUGGESTIONS = listOf(
    "Give me a recipe for pasta",
    "Add eggs and milk to my list",
    "What's on my list?",
)

@Composable
fun ChatScreen(
    onClose: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()
    val liveSteps by viewModel.liveSteps.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val addedRecipeIds by viewModel.addedRecipeIds.collectAsStateWithLifecycle()

    // Slide-in from right animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val translationX by animateFloatAsState(
        targetValue = if (visible) 0f else screenWidth,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "chat_slide_in",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.translationX = translationX }
            .background(Color.White),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(HeaderBg),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .background(BackButtonBg, CircleShape),
                    ) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "Back",
                            tint = Color(0xFF080816),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Food & grocery assistant",
                        fontSize = 11.sp,
                        color = SubtitleColor,
                        fontWeight = FontWeight.Normal,
                    )
                }
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
            }

            // Body
            if (messages.isEmpty() && !isLoading) {
                WelcomeState(
                    suggestions = SUGGESTIONS,
                    onSuggestion = viewModel::sendMessage,
                    modifier = Modifier.weight(1f),
                )
            } else {
                MessageList(
                    messages = messages,
                    streamingText = streamingText,
                    liveSteps = liveSteps,
                    isLoading = isLoading,
                    onAddIngredients = viewModel::handleAddIngredients,
                    addedRecipeIds = addedRecipeIds,
                    onSuggestion = viewModel::sendMessage,
                    modifier = Modifier.weight(1f),
                )
            }

            // Footer
            ChatInput(
                onSend = viewModel::sendMessage,
                onAbort = viewModel::abort,
                isLoading = isLoading,
            )
        }
    }
}
