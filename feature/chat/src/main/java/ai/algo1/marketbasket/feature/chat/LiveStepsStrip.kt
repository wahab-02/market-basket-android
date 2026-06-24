package ai.algo1.marketbasket.feature.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiveStepsStrip(steps: List<LiveStep>, modifier: Modifier = Modifier) {
    if (steps.isEmpty()) return
    Column(modifier = modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        steps.forEach { step -> StepPill(step) }
    }
}

@Composable
private fun StepPill(step: LiveStep) {
    when (step.status) {
        StepStatus.Pending -> PendingPill(step.label)
        StepStatus.Running -> RunningPill(step.inputSummary ?: step.label)
        StepStatus.Done    -> DonePill(step.outputSummary ?: step.label)
        StepStatus.Error   -> ErrorPill(step.outputSummary ?: step.label)
    }
}

@Composable
private fun PendingPill(label: String) {
    val transition = rememberInfiniteTransition(label = "pending_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pending_alpha",
    )
    PillRow(bgColor = Color(0xFFF3F4F6), textColor = Color(0xFF6B7280), modifier = Modifier.alpha(alpha)) {
        Box(Modifier.size(8.dp).background(Color(0xFFD1D5DB), CircleShape))
        Text("$label…", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
    }
}

@Composable
private fun RunningPill(label: String) {
    val transition = rememberInfiniteTransition(label = "running_spin")
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
        label = "running_rotation",
    )
    PillRow(bgColor = Color(0xFFEFF6FF), textColor = Color(0xFF2563EB)) {
        Box(
            Modifier.size(10.dp).rotate(rotation)
                .background(Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // Spinner ring drawn via inner/outer contrast
            Box(Modifier.size(10.dp).background(Color(0xFF2563EB), CircleShape))
            Box(Modifier.size(6.dp).background(Color(0xFFEFF6FF), CircleShape))
        }
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2563EB))
    }
}

@Composable
private fun DonePill(label: String) {
    PillRow(bgColor = Color(0xFFF0FDF4), textColor = Color(0xFF15803D)) {
        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF15803D))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF15803D))
    }
}

@Composable
private fun ErrorPill(label: String) {
    PillRow(bgColor = Color(0xFFFEF2F2), textColor = Color(0xFFDC2626)) {
        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626))
    }
}

@Composable
private fun PillRow(
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}
