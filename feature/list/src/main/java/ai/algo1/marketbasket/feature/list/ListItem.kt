package ai.algo1.marketbasket.feature.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val SpringEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

@Composable
fun ListItem(
    item: GroceryItem,
    expanded: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onIncrement: () -> Unit = {},
    onDecrement: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val bounceScale = remember { Animatable(1f) }
    val imageSize by animateDpAsState(
        targetValue = if (expanded) 56.dp else 48.dp,
        animationSpec = tween(220),
        label = "img_size",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .animateContentSize(tween(220)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(imageSize).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier = Modifier.size(imageSize).clip(RoundedCornerShape(12.dp))
                        .background(MarketBasketColors.ImagePlaceholder),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        item.name.take(1).uppercase(),
                        color = MarketBasketColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    color = MarketBasketColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                )
                val source = item.source
                AnimatedVisibility(
                    visible = expanded && !source.isNullOrBlank(),
                    enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                    exit = shrinkVertically(tween(150)) + fadeOut(tween(120)),
                ) {
                    if (!source.isNullOrBlank()) {
                        Text(
                            text = "Added by ${source.replaceFirstChar { it.uppercase() }}",
                            fontSize = 13.sp,
                            color = MarketBasketColors.TextSecondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }

            // Qty control: small grey circle when collapsed, full pill when expanded
            AnimatedContent(
                targetState = expanded,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "qty_control",
            ) { isExpanded ->
                if (isExpanded) {
                    ExpandedQtyControl(
                        qty = item.quantity ?: 1,
                        onDecrement = {
                            if ((item.quantity ?: 1) <= 1) onDelete() else onDecrement()
                        },
                        onIncrement = onIncrement,
                    )
                } else {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                            .background(MarketBasketColors.BackgroundAlt),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${item.quantity ?: 1}", fontSize = 13.sp, color = MarketBasketColors.TextPrimary)
                    }
                }
            }

            // Checkbox with bounce + faded checkmark when unchecked
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .scale(bounceScale.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        scope.launch {
                            bounceScale.snapTo(1f)
                            bounceScale.animateTo(0.72f, tween(106, easing = SpringEasing))
                            bounceScale.animateTo(1.22f, tween(114, easing = SpringEasing))
                            bounceScale.animateTo(0.94f, tween(84, easing = SpringEasing))
                            bounceScale.animateTo(1f, tween(76, easing = SpringEasing))
                        }
                        onToggle()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(28.dp)) {
                    if (item.checked) {
                        drawCircle(color = Color(0xFF2E7D32))
                    } else {
                        drawCircle(
                            color = Color(0xFFBBBBBB),
                            radius = size.minDimension / 2f - 2.dp.toPx(),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)),
                            ),
                        )
                    }
                }
                Icon(
                    Icons.Filled.Check,
                    contentDescription = if (item.checked) "Checked" else "Unchecked",
                    tint = if (item.checked) Color.White else Color(0xFFBBBBBB),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 88.dp)
                .background(Color(0xFFF0F0F0)),
        )
    }
}

@Composable
private fun ExpandedQtyControl(qty: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(MarketBasketColors.BackgroundAlt)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // Left: trash when qty=1, minus when qty>1
        Box(
            modifier = Modifier.size(28.dp).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDecrement,
            ),
            contentAlignment = Alignment.Center,
        ) {
            if (qty <= 1) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remove",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp),
                )
            } else {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val cy = size.height / 2f
                    val len = 5.dp.toPx()
                    drawLine(
                        Color(0xFFD71920),
                        start = Offset(size.width / 2f - len, cy),
                        end = Offset(size.width / 2f + len, cy),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        // Center: qty badge
        Box(
            modifier = Modifier.size(26.dp).clip(CircleShape).background(Color(0xFF080816)),
            contentAlignment = Alignment.Center,
        ) {
            Text("$qty", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        // Right: plus
        Box(
            modifier = Modifier.size(28.dp).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onIncrement,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val len = 5.dp.toPx()
                val sw = 2.5.dp.toPx()
                drawLine(Color(0xFFD71920), Offset(cx - len, cy), Offset(cx + len, cy), sw, StrokeCap.Round)
                drawLine(Color(0xFFD71920), Offset(cx, cy - len), Offset(cx, cy + len), sw, StrokeCap.Round)
            }
        }
    }
}
