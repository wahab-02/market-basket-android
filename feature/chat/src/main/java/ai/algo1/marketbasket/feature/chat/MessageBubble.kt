package ai.algo1.marketbasket.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import ai.algo1.marketbasket.feature.chat.artifacts.RecipeCard

private val UserBubbleBg      = Color(0xFF080816)
private val AssistantBubbleBg = Color(0xFFF8F9FB)
private val BrandInk          = Color(0xFF080816)
private val ChipBg            = Color(0xFFFFFFFF)
private val ChipBorder        = Color(0xFFE8E9ED)
private val ChipText          = Color(0xFF374151)

@Composable
fun MessageBubble(
    message: ChatMessage,
    streamingText: String? = null,
    isStreaming: Boolean = false,
    liveSteps: List<LiveStep> = emptyList(),
    onAddIngredients: (messageId: String, ingredients: List<String>) -> Unit,
    addedRecipeIds: Set<String>,
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    if (message.role == Role.User) {
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .widthIn(max = screenWidth * 0.80f)
                    .background(
                        UserBubbleBg,
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(message.text, color = Color.White, fontSize = 15.sp)
            }
        }
        return
    }

    // Assistant bubble
    val displayText = if (isStreaming) (streamingText ?: "") else message.text
    val displaySteps = if (isStreaming) liveSteps else message.steps.filter { it.status != StepStatus.Done }

    Column(modifier = modifier.fillMaxWidth().widthIn(max = screenWidth * 0.92f)) {
        if (displaySteps.isNotEmpty()) {
            LiveStepsStrip(steps = displaySteps, modifier = Modifier.padding(bottom = 4.dp))
        }

        if (displayText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .background(
                        AssistantBubbleBg,
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (isStreaming) {
                    // Plain text while streaming — no markdown parsing mid-stream
                    Text(
                        text = displayText,
                        color = BrandInk,
                        fontSize = 15.sp,
                    )
                } else {
                    Markdown(
                        content = displayText,
                        colors = markdownColor(text = BrandInk),
                        typography = markdownTypography(
                            text = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
                        ),
                    )
                }
            }
        }

        // Artifacts
        message.artifacts.forEach { artifact ->
            when (artifact) {
                is Artifact.Recipe -> RecipeCard(
                    artifact = artifact,
                    addedToList = addedRecipeIds.contains(message.id),
                    onAddIngredients = { ings -> onAddIngredients(message.id, ings) },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // Suggestion chips
        if (!isStreaming && message.suggestions.isNotEmpty()) {
            SuggestionChips(
                suggestions = message.suggestions,
                onSuggestion = onSuggestion,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionChips(
    suggestions: List<String>,
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        suggestions.forEach { s ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ChipBg)
                    .clickable { onSuggestion(s) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(s, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ChipText)
            }
        }
    }
}
