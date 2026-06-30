package ai.algo1.marketbasket.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val NotificationGreen = Color(0xFF008C7A)
private val NotificationGreenTint = Color(0xFFE5FBEF)
private val NotificationText = Color(0xFF111111)
private val NotificationEnterEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private const val NotificationDurationMillis = 2_200L

data class AddItemNotification(
    val id: Long,
    val message: String,
)

class AddItemNotificationState {
    var current: AddItemNotification? by mutableStateOf(null)
        private set

    private var nextId = 0L

    fun showItemsAdded(message: String): AddItemNotification {
        val notification = AddItemNotification(id = ++nextId, message = message)
        current = notification
        return notification
    }

    fun dismiss(id: Long) {
        if (current?.id == id) {
            current = null
        }
    }
}

@Composable
internal fun AddItemNotificationHost(
    notification: AddItemNotification?,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(notification?.id) {
        val id = notification?.id ?: return@LaunchedEffect
        delay(NotificationDurationMillis)
        onDismiss(id)
    }

    AnimatedVisibility(
        visible = notification != null,
        modifier = modifier.statusBarsPadding().padding(top = 12.dp),
        enter = slideInVertically(
            animationSpec = tween(220, easing = NotificationEnterEasing),
            initialOffsetY = { -it },
        ) + fadeIn(animationSpec = tween(140)),
        exit = slideOutVertically(
            animationSpec = tween(160),
            targetOffsetY = { -it / 2 },
        ) + fadeOut(animationSpec = tween(120)),
    ) {
        notification?.let { visibleNotification ->
            val isListNotification = visibleNotification.message == "Item Added"
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .then(if (isListNotification) Modifier else Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(0.dp),
                        ambientColor = Color(0x26000000),
                        spotColor = Color(0x26000000),
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = if (isListNotification) 12.dp else 24.dp,
                        top = if (isListNotification) 10.dp else 10.dp,
                        end = if (isListNotification) 20.dp else 24.dp,
                        bottom = if (isListNotification) 10.dp else 10.dp,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isListNotification) 46.dp else 38.dp)
                            .background(if (isListNotification) NotificationGreen else NotificationGreenTint),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (isListNotification) Color.White else NotificationGreen,
                            modifier = Modifier.size(if (isListNotification) 24.dp else 20.dp),
                        )
                    }
                    Text(
                        text = visibleNotification.message,
                        color = if (isListNotification) NotificationGreen else NotificationText,
                        fontSize = if (isListNotification) 16.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = if (isListNotification) 14.dp else 16.dp),
                    )
                }
            }
        }
    }
}
