package ai.algo1.marketbasket.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val InputBg    = Color(0xFFF3F4F6)
private val BrandInk   = Color(0xFF080816)
private val PlaceColor = Color(0xFF9CA3AF)

@Composable
fun ChatInput(
    onSend: (String) -> Unit,
    onAbort: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp, top = 8.dp)
            .background(InputBg, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        // Text field
        Box(modifier = Modifier.weight(1f).heightIn(min = 36.dp, max = 120.dp)) {
            if (text.isEmpty()) {
                Text(
                    "Ask about recipes, your list…",
                    color = PlaceColor,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(color = BrandInk, fontSize = 15.sp),
                cursorBrush = SolidColor(BrandInk),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Send / Stop button
        val canSend = text.isNotBlank() && !isLoading
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandInk)
                .alpha(if (isLoading || canSend) 1f else 0.3f)
                .clickable(enabled = isLoading || canSend) {
                    if (isLoading) {
                        onAbort()
                    } else {
                        val trimmed = text.trim()
                        if (trimmed.isNotBlank()) {
                            text = ""
                            onSend(trimmed)
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                // Stop icon: white square
                Box(Modifier.size(12.dp).background(Color.White, RoundedCornerShape(2.dp)))
            } else {
                // Send icon: arrow drawn via Canvas
                SendArrow()
            }
        }
    }
}

@Composable
private fun SendArrow() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            // Arrow pointing up-right
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.55f)
            lineTo(w * 0.62f, h * 0.55f)
            lineTo(w * 0.62f, h * 0.85f)
            lineTo(w * 0.38f, h * 0.85f)
            lineTo(w * 0.38f, h * 0.55f)
            lineTo(w * 0.15f, h * 0.55f)
            close()
        }
        drawPath(path, color = androidx.compose.ui.graphics.Color.White)
    }
}
