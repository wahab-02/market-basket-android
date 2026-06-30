package ai.algo1.marketbasket.feature.ideas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MealPlanRed = Color(0xFFB83040)
private val MealPlanTextDark = Color(0xFF111111)
private val MealPlanTextGray = Color(0xFF888888)
private val MealPlanBorder = Color(0xFFDDDDDD)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MealPlanningTab(onNavigateToRecipes: () -> Unit = {}) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d", Locale.US) }
    val todayDate = remember { dateFormatter.format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.statusBarsPadding().height(80.dp))

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawRect(color = MealPlanBorder, style = Stroke(width = 1.dp.toPx()))
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Back",
                        tint = MealPlanTextDark,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = "Add Recipe",
                        color = MealPlanRed,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = todayDate,
                        color = MealPlanTextGray,
                        fontSize = 14.sp,
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            Spacer(Modifier.height(24.dp))

            FilterSection(
                icon = Icons.Outlined.Language,
                title = "Cuisine",
                options = listOf("Italian", "Mexican", "Asian", "American", "Indian", "French", "Mediterranean", "Thai"),
            )

            Spacer(Modifier.height(28.dp))

            FilterSection(
                icon = Icons.Outlined.DinnerDining,
                title = "Meal",
                options = listOf("Breakfast", "Lunch", "Dinner", "Snacks"),
                defaultSelected = setOf("Dinner", "Snacks"),
            )

            Spacer(Modifier.height(28.dp))

            FilterSection(
                icon = Icons.Outlined.AccessTime,
                title = "Preparation Time",
                options = listOf("15 mins or less", "30 mins", "45 mins", "1 hour", "1-2 hours"),
                defaultSelected = setOf("1-2 hours"),
            )

            Spacer(Modifier.height(32.dp))

            Surface(
                onClick = onNavigateToRecipes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                color = MealPlanRed,
                shape = RoundedCornerShape(0.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Plan My Meals",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.navigationBarsPadding().height(120.dp))
        }

        MealPlanAskBasketButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 28.dp, bottom = 104.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    icon: ImageVector,
    title: String,
    options: List<String>,
    defaultSelected: Set<String> = emptySet(),
) {
    var selected by remember { mutableStateOf(defaultSelected) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MealPlanTextDark,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = title,
                color = MealPlanTextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(14.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                val isSelected = option in selected
                MealPlanChip(
                    label = option,
                    selected = isSelected,
                    onClick = {
                        selected = if (isSelected) selected - option else selected + option
                    },
                )
            }
        }
    }
}

@Composable
private fun MealPlanChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) MealPlanRed else Color.White,
        shape = RoundedCornerShape(0.dp),
        border = if (selected) null else BorderStroke(1.dp, MealPlanBorder),
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MealPlanTextDark,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun MealPlanAskBasketButton(modifier: Modifier = Modifier) {
    val floatTransition = rememberInfiniteTransition(label = "ask_basket_float")
    val translateY by floatTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1900), repeatMode = RepeatMode.Reverse),
        label = "ask_basket_translate_y",
    )
    val pulseScale by floatTransition.animateFloat(
        initialValue = 0.82f, targetValue = 1.16f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1700), repeatMode = RepeatMode.Reverse),
        label = "ask_basket_pulse_scale",
    )
    val pulseAlpha by floatTransition.animateFloat(
        initialValue = 0.22f, targetValue = 0.08f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1700), repeatMode = RepeatMode.Reverse),
        label = "ask_basket_pulse_alpha",
    )

    Column(
        modifier = modifier.width(86.dp).graphicsLayer { translationY = translateY },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                    .alpha(pulseAlpha)
                    .background(Color(0xFF34D399), CircleShape),
            )
            Image(
                painter = painterResource(R.drawable.mb),
                contentDescription = "Ask Basket",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(72.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            )
        }
        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .graphicsLayer { translationY = -10f }
                .shadow(10.dp, RoundedCornerShape(999.dp), ambientColor = Color(0x2E000000), spotColor = Color(0x2E000000)),
        ) {
            Text(
                text = "Ask Basket",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            )
        }
    }
}
