package ai.algo1.marketbasket.nav

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.R

internal val primaryBottomNavDestinations = listOf(
    Destination.Today,
    Destination.List,
    Destination.Deals,
    Destination.Ideas,
)

internal val profileBottomNavDestination = Destination.You

private val BrandRed = Color(0xFFD71920)
private val BrandInk = Color(0xFF080816)
private val AppBackground = Color(0xFFF7F7F7)
private val HeaderButtonBorder = Color(0xFFF2F2F2)
private val NavPillEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val NavPopEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

private data class DrawerOption(
    val id: String,
    val label: String,
    val subtitle: String,
    val iconRes: Int? = null,
    val icon: ImageVector? = null,
    val iconText: String? = null,
    val iconBackground: Color,
    val iconTint: Color = Color.White,
    val connected: Boolean? = null,
    val dividerBefore: Boolean = false,
    val isLogout: Boolean = false,
    val disabled: Boolean = false,
)

private val drawerOptions = listOf(
    DrawerOption(
        id = "scan",
        label = "Scan List",
        subtitle = "Scan paper list to add items",
        iconRes = R.drawable.scan,
        iconBackground = Color.Transparent,
    ),
    DrawerOption(
        id = "family",
        label = "Add Family Member",
        subtitle = "Share your list with family",
        iconRes = R.drawable.family,
        iconBackground = Color.Transparent,
    ),
    DrawerOption(
        id = "whatsapp",
        label = "Connect with WhatsApp",
        subtitle = "Connect with WhatsApp",
        iconRes = R.drawable.whatsapp,
        iconBackground = Color.Transparent,
        connected = true,
        dividerBefore = true,
    ),
    DrawerOption(
        id = "slack",
        label = "Slack",
        subtitle = "Connect with Slack",
        iconRes = R.drawable.slack,
        iconBackground = Color.Transparent,
        connected = false,
    ),
    DrawerOption(
        id = "alexa",
        label = "Amazon Alexa",
        subtitle = "Connect with Amazon Alexa",
        iconRes = R.drawable.alexa_icon,
        iconBackground = Color.Transparent,
    ),
    DrawerOption(
        id = "chatgpt",
        label = "ChatGPT",
        subtitle = "Connect with ChatGPT",
        iconRes = R.drawable.chatgpt,
        iconBackground = Color.Transparent,
    ),
    DrawerOption(
        id = "claude",
        label = "Claude",
        subtitle = "Connect with Claude",
        iconRes = R.drawable.claude,
        iconBackground = Color.Transparent,
    ),
    DrawerOption(
        id = "google",
        label = "Google Assistant",
        subtitle = "Connect your Google Account",
        iconRes = R.drawable.google_assistant_logo,
        iconBackground = Color.Transparent,
    ),
    DrawerOption(
        id = "siri",
        label = "Siri Shortcuts",
        subtitle = "Add items with Siri",
        iconRes = R.drawable.siri,
        iconBackground = Color.Transparent,
    ),
    DrawerOption(
        id = "logout",
        label = "Logout",
        subtitle = "Sign out of your account",
        icon = Icons.Filled.Logout,
        iconBackground = Color(0xFFFEE2E2),
        iconTint = Color(0xFFEF4444),
        dividerBefore = true,
        isLogout = true,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConnectionsBottomDrawer(
    open: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!open) return

    ModalBottomSheet(
        onDismissRequest = onClose,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(40.dp).height(4.dp).background(Color(0xFFD1D5DB), RoundedCornerShape(999.dp)))
                Surface(
                    color = Color(0xFFE5E7EB),
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(32.dp).clickable(onClick = onClose),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close menu", tint = Color(0xFF666666), modifier = Modifier.padding(8.dp))
                }
            }
        },
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 28.dp)) {
            Box(Modifier.fillMaxWidth().padding(bottom = 20.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.market_basket),
                    contentDescription = "Market Basket",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(192.dp).height(30.dp),
                )
            }

            drawerOptions.groupByDivider().forEach { group ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x14080816), spotColor = Color(0x14080816)),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        group.forEachIndexed { index, option ->
                            DrawerOptionRow(
                                option = option,
                                showDivider = index != group.lastIndex,
                                onClick = onClose,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerOptionRow(option: DrawerOption, showDivider: Boolean, onClick: () -> Unit) {
    val textColor = when {
        option.disabled -> Color(0xFF9CA3AF)
        option.isLogout -> Color(0xFFEF4444)
        else -> Color(0xFF111827)
    }
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        color = if (option.disabled) Color(0xFFF9FAFB) else Color.White,
        modifier = Modifier.fillMaxWidth().clickable(interactionSource = interactionSource, indication = null, enabled = !option.disabled, onClick = onClick),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DrawerOptionIcon(option)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(option.label, color = textColor, fontSize = 19.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
                    when {
                        option.connected == true -> ConnectedStatus()
                        else -> Text(option.subtitle, color = Color(0xFF9CA3AF), fontSize = 15.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium)
                    }
                }
                DrawerOptionTrailing(option)
            }
            if (showDivider) {
                Box(Modifier.fillMaxWidth().padding(start = 88.dp).height(1.dp).background(Color(0xFFF3F4F6)))
            }
        }
    }
}

@Composable
private fun DrawerOptionIcon(option: DrawerOption) {
    Surface(color = option.iconBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (option.iconRes != null) {
                Image(painter = painterResource(option.iconRes), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            } else if (option.icon != null) {
                Icon(option.icon, contentDescription = null, tint = option.iconTint, modifier = Modifier.size(24.dp))
            } else {
                Text(option.iconText.orEmpty(), color = option.iconTint, fontSize = if ((option.iconText?.length ?: 0) > 1) 12.sp else 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ConnectedStatus() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(12.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(12.dp).background(Color(0xFF86EFAC), CircleShape))
            Box(Modifier.size(6.dp).background(Color(0xFF16A34A), CircleShape))
        }
        Text("Connected", color = Color(0xFF16A34A), fontSize = 15.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DrawerOptionTrailing(option: DrawerOption) {
    when {
        option.disabled -> Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(24.dp))
        option.connected == true -> ToggleSwitch(on = true)
        else -> Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = if (option.isLogout) Color(0xFFEF4444) else Color(0xFFCCCCCC), modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun ToggleSwitch(on: Boolean) {
    Surface(color = if (on) Color(0xFF34C759) else Color(0xFFE5E7EB), shape = RoundedCornerShape(999.dp), modifier = Modifier.width(48.dp).height(28.dp)) {
        Box(Modifier.fillMaxSize().padding(2.dp), contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(Modifier.size(24.dp).background(Color.White, CircleShape).shadow(2.dp, CircleShape))
        }
    }
}

private fun List<DrawerOption>.groupByDivider(): List<List<DrawerOption>> {
    val groups = mutableListOf<MutableList<DrawerOption>>()
    forEach { option ->
        if (groups.isEmpty() || option.dividerBefore) {
            groups += mutableListOf(option)
        } else {
            groups.last() += option
        }
    }
    return groups
}

@Composable
internal fun MarketBasketHeader(
    modifier: Modifier = Modifier,
    greeting: String = "Good evening, Shopper",
    onSearchClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    Surface(color = Color.White, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = greeting, color = BrandInk, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFC233),
                        modifier = Modifier.size(16.dp),
                    )
                }
                Image(
                    painter = painterResource(R.drawable.market_basket),
                    contentDescription = "Market Basket",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(top = 12.dp).width(238.dp).height(35.dp),
                )
            }
            Row(
                modifier = Modifier.padding(top = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HeaderActionButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = BrandInk, modifier = Modifier.size(24.dp))
                }
                HeaderActionButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = BrandRed, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

@Composable
internal fun MarketBasketCompactHeader(
    modifier: Modifier = Modifier,
    greeting: String = "Good evening, Shopper",
    onSearchClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    Surface(color = Color.White, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 20.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = greeting, color = BrandInk, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                Icon(
                    imageVector = Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFFC233),
                    modifier = Modifier.size(16.dp),
                )
            }
            HeaderActionButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = BrandRed, modifier = Modifier.size(24.dp))
            }
            HeaderActionButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = BrandRed, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
internal fun FloatingChatLauncher(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val floatTransition = rememberInfiniteTransition(label = "ask_basket_float")
    val translateY = floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1900), repeatMode = RepeatMode.Reverse),
        label = "ask_basket_translate_y",
    )
    val pulseScale = floatTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1700), repeatMode = RepeatMode.Reverse),
        label = "ask_basket_pulse_scale",
    )
    val pulseAlpha = floatTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1700), repeatMode = RepeatMode.Reverse),
        label = "ask_basket_pulse_alpha",
    )

    Column(
        modifier = modifier.width(86.dp).graphicsLayer { translationY = translateY.value },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(72.dp)
                    .graphicsLayer {
                        scaleX = pulseScale.value
                        scaleY = pulseScale.value
                    }
                    .alpha(pulseAlpha.value)
                    .background(Color(0xFF34D399), CircleShape),
            )
            Image(
                painter = painterResource(R.drawable.mb),
                contentDescription = "Ask Basket",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(72.dp).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            )
        }
        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.graphicsLayer { translationY = -10f }
                .shadow(10.dp, RoundedCornerShape(999.dp), ambientColor = Color(0x2E000000), spotColor = Color(0x2E000000)),
        ) {
            Text(
                "Ask Basket",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun HeaderActionButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState().value
    val scale = animateFloatAsState(if (pressed) 0.95f else 1f, label = "header_button_press_scale")
    Surface(
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, HeaderButtonBorder),
        modifier = Modifier.size(42.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(18.dp, CircleShape, ambientColor = Color(0x33080816), spotColor = Color(0x33080816))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
internal fun FloatingBottomNav(
    selected: Destination,
    onDestinationClick: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    inverted: Boolean = false,
) {
    val pillBg = if (inverted) Color(0xFF1A1A1A).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.86f)
    val pillBorder = if (inverted) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.72f)
    val innerBg1 = if (inverted) Color(0xFF1A1A1A).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.88f)
    val innerBg2 = if (inverted) Color(0xFF1A1A1A).copy(alpha = 0.80f) else Color.White.copy(alpha = 0.62f)
    val innerBg3 = if (inverted) Color(0xFF1A1A1A).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.80f)
    val selectedPillBg = if (inverted) Color.White else Color.White

    val selectedPrimaryIndex = primaryBottomNavDestinations.indexOf(selected)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = pillBg,
            shape = RoundedCornerShape(35.dp),
            border = BorderStroke(1.dp, pillBorder),
            modifier = Modifier.weight(1f).fillMaxHeight().shadow(40.dp, RoundedCornerShape(35.dp), ambientColor = Color(0x33080816), spotColor = Color(0x33080816)),
        ) {
            BoxWithConstraints(Modifier.fillMaxSize().padding(4.dp)) {
                val itemWidth = maxWidth / primaryBottomNavDestinations.size
                val pillOffset = animateDpAsState(
                    targetValue = if (selectedPrimaryIndex >= 0) itemWidth * selectedPrimaryIndex.toFloat() else 0.dp,
                    animationSpec = tween(500, easing = NavPillEasing),
                    label = "bottom_nav_pill_offset",
                )
                val pillAlpha = animateFloatAsState(
                    targetValue = if (selectedPrimaryIndex >= 0) 1f else 0f,
                    animationSpec = tween(180),
                    label = "bottom_nav_pill_alpha",
                )
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(
                            Brush.verticalGradient(colors = listOf(innerBg1, innerBg2, innerBg3)),
                            RoundedCornerShape(31.dp),
                        ),
                )
                Box(
                    modifier = Modifier.offset(x = pillOffset.value)
                        .width(itemWidth)
                        .fillMaxHeight()
                        .alpha(pillAlpha.value)
                        .shadow(30.dp, RoundedCornerShape(31.dp), ambientColor = Color(0x3D080816), spotColor = Color(0x3D080816))
                        .background(selectedPillBg, RoundedCornerShape(31.dp)),
                )
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    primaryBottomNavDestinations.forEach { destination ->
                        FloatingNavItem(
                            destination = destination,
                            selected = selected == destination,
                            onClick = { onDestinationClick(destination) },
                            modifier = Modifier.weight(1f),
                            inverted = inverted,
                        )
                    }
                }
            }
        }

        FloatingProfileNavItem(
            selected = selected == profileBottomNavDestination,
            onClick = { onDestinationClick(profileBottomNavDestination) },
            inverted = inverted,
        )
    }
}

@Composable
private fun FloatingNavItem(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    inverted: Boolean = false,
) {
    val foreground = animateColorAsState(
        targetValue = when {
            selected && inverted -> BrandInk
            selected -> BrandRed
            inverted -> Color.White.copy(alpha = 0.72f)
            else -> BrandInk.copy(alpha = 0.72f)
        },
        animationSpec = tween(300),
        label = "bottom_nav_item_color",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState().value
    val targetScale = when {
        pressed -> 0.97f
        selected -> 1.06f
        else -> 1f
    }
    val pressScale = animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(260, easing = NavPopEasing),
        label = "bottom_nav_item_press_scale",
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SourceBottomNavIcon(destination = destination, tint = foreground.value, selected = selected)
        Spacer(Modifier.height(1.dp))
        Text(
            destination.label,
            color = foreground.value,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun FloatingProfileNavItem(selected: Boolean, onClick: () -> Unit, inverted: Boolean = false) {
    val foreground = when {
        selected && inverted -> Color.White
        selected -> BrandRed
        inverted -> Color.White.copy(alpha = 0.72f)
        else -> BrandInk.copy(alpha = 0.72f)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState().value
    val pressScale = animateFloatAsState(if (pressed) 0.97f else 1f, label = "profile_nav_press_scale")
    Surface(
        shape = CircleShape,
        color = if (inverted) Color(0xFF1A1A1A).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, if (inverted) Color.White.copy(alpha = 0.18f) else if (selected) Color.White else Color(0xFFE1E1E1)),
        modifier = Modifier.size(64.dp)
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
            }
            .shadow(30.dp, CircleShape, ambientColor = Color(0x1F080816), spotColor = Color(0x1F080816))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        Column(
            Modifier.fillMaxSize().padding(vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SourceBottomNavIcon(destination = profileBottomNavDestination, tint = foreground, selected = selected)
            Spacer(Modifier.height(1.dp))
            Text(
                profileBottomNavDestination.label,
                color = foreground,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SourceBottomNavIcon(destination: Destination, tint: Color, selected: Boolean) {
    val imageRes = sourceBottomNavImageRes(destination)
    if (imageRes != null) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(tint),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(24.dp).graphicsLayer {
                val scale = if (selected) 1.05f else 1f
                scaleX = scale
                scaleY = scale
            },
        )
        return
    }

    val imageVector = remember(destination, tint) {
        when (destination) {
            Destination.Today -> homeNavIcon(tint)
            Destination.List -> listNavIcon(tint)
            Destination.Deals -> dealsNavIcon(tint)
            Destination.Ideas -> ideasNavIcon(tint)
            Destination.You -> youNavIcon(tint)
        }
    }
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(24.dp).graphicsLayer {
            val scale = if (selected) 1.04f else 1f
            scaleX = scale
            scaleY = scale
        },
    )
}

internal fun sourceBottomNavImageRes(destination: Destination): Int? =
    when (destination) {
        Destination.Deals -> R.drawable.deals
        Destination.Ideas -> R.drawable.recipes
        else -> null
    }

private fun homeNavIcon(color: Color): ImageVector =
    ImageVector.Builder(defaultWidth = 28.dp, defaultHeight = 28.dp, viewportWidth = 34f, viewportHeight = 34f).apply {
        addPath(pathData = PathParser().parsePathString("M7.5 16.2 17 8.4l9.5 7.8v10.1a1.7 1.7 0 0 1-1.7 1.7h-5.2v-7.2h-5.2V28H9.2a1.7 1.7 0 0 1-1.7-1.7V16.2Z").toNodes(), fill = SolidColor(color))
    }.build()

private fun listNavIcon(color: Color): ImageVector =
    ImageVector.Builder(defaultWidth = 28.dp, defaultHeight = 28.dp, viewportWidth = 34f, viewportHeight = 34f).apply {
        addPath(
            pathData = PathParser().parsePathString("M6.5 10.5l2.2 2.2 4-4M6.5 18l2.2 2.2 4-4M6.5 25.5l2.2 2.2 4-4").toNodes(),
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = 3.2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
        )
        addPath(
            pathData = PathParser().parsePathString("M17 10.5h10.5M17 18h10.5M17 25.5h10.5").toNodes(),
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = 3.2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }.build()

private fun dealsNavIcon(color: Color): ImageVector =
    ImageVector.Builder(defaultWidth = 28.dp, defaultHeight = 28.dp, viewportWidth = 20f, viewportHeight = 20f).apply {
        addPath(pathData = PathParser().parsePathString("M10.5133 0.0147702C10.855 -0.0124573 11.436 0.00616447 11.7988 0.0068338L14.1711 0.0114713L16.0839 0.0154395C17.2196 0.0169455 18.0035 -0.0590714 18.7424 0.967135C19.2427 1.66195 19.1522 2.58197 19.1575 3.3971L19.1798 5.49974L19.2144 7.52332C19.2186 7.80924 19.2416 8.3721 19.1997 8.63448C19.0835 8.80038 18.6831 9.13381 18.5176 9.30219L16.5312 11.3192L8.69348 19.2761C8.39386 19.0503 8.21271 18.798 7.94567 18.5591C7.56559 18.219 7.24144 17.8743 6.88358 17.5157L3.79088 14.4584C2.75215 13.4276 1.69561 12.4123 0.662185 11.3761C0.43105 11.1443 0.191477 10.9582 0 10.6879C0.0293311 10.6101 0.12397 10.5149 0.183469 10.4538L6.81895 3.73796C7.79706 2.72031 8.78858 1.71564 9.79324 0.724215C10.0549 0.46994 10.212 0.220638 10.5133 0.0147702ZM16.9661 1.33108C16.7162 1.3652 16.5008 1.52423 16.3947 1.75305C16.2886 1.98187 16.3063 2.24898 16.4417 2.4618C16.5959 2.7042 16.8765 2.83546 17.1614 2.79852C17.5678 2.74586 17.854 2.37304 17.8 1.96683C17.7459 1.56062 17.3721 1.27563 16.9661 1.33108Z").toNodes(), fill = SolidColor(color))
    }.build()

private fun ideasNavIcon(color: Color): ImageVector =
    ImageVector.Builder(defaultWidth = 28.dp, defaultHeight = 28.dp, viewportWidth = 34f, viewportHeight = 34f).apply {
        addPath(
            pathData = PathParser().parsePathString("M12 6v22M8.5 6v7.5c0 2 1.6 3.5 3.5 3.5s3.5-1.5 3.5-3.5V6M23 6v22M23 6c3 2.2 4.5 5.2 4.5 9v2.5H23").toNodes(),
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = 3f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }.build()

private fun youNavIcon(color: Color): ImageVector =
    ImageVector.Builder(defaultWidth = 28.dp, defaultHeight = 28.dp, viewportWidth = 34f, viewportHeight = 34f).apply {
        addPath(
            pathData = PathParser().parsePathString("M17 17a5.5 5.5 0 1 0 0-11 5.5 5.5 0 0 0 0 11Z").toNodes(),
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = 3f,
        )
        addPath(
            pathData = PathParser().parsePathString("M7.5 28c1.6-5 5-7.5 9.5-7.5s7.9 2.5 9.5 7.5").toNodes(),
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = 3f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }.build()

internal fun Destination.showsMarketBasketHeader(): Boolean = this == Destination.Today || this == Destination.List

internal fun Destination.showsCompactHeader(): Boolean = this == Destination.Deals

internal val marketBasketAppBackground: Color = AppBackground
