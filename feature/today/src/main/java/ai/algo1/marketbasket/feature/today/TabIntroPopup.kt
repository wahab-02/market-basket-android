package ai.algo1.marketbasket.feature.today

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class TabIntroPopupKind {
    Today,
    Ideas,
}

@Composable
fun TabIntroPopup(
    kind: TabIntroPopupKind?,
    onDismiss: () -> Unit,
) {
    if (kind == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val scrimInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
                .clickable(scrimInteraction, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                val sheetHeight = maxHeight * 0.82f
                val sheetInteraction = remember { MutableInteractionSource() }
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(sheetHeight)
                        .clickable(sheetInteraction, indication = null, onClick = {}),
                ) {
                    when (kind) {
                        TabIntroPopupKind.Today -> TodayIntroSheet(onDismiss = onDismiss)
                        TabIntroPopupKind.Ideas -> IdeasIntroSheet(onDismiss = onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayIntroSheet(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.home_popup),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.72f to Color.White.copy(alpha = 0.2f),
                        1f to Color.White,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp, top = 36.dp, end = 130.dp),
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = PopupBlue)) { append("One list,\n") }
                        withStyle(SpanStyle(color = PopupRed)) { append("Everything\nconnected.") }
                    },
                    fontSize = 28.sp,
                    lineHeight = 33.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    "Market Basket brings your list, deals, recipes, and ideas together so you can save money, time and eat better.",
                    color = PopupBody,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    modifier = Modifier.padding(top = 28.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.home_popup_list),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 185.dp),
            )
            Spacer(Modifier.height(58.dp))
        }
        CloseButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 14.dp),
        )
        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PopupBlue),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Got it", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun IdeasIntroSheet(onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Box(Modifier.fillMaxSize().background(Color.White)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> IdeaLinkSlide(onDismiss = onDismiss)
                else -> IdeaOrganizedSlide(onDismiss = onDismiss)
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .width(if (pagerState.currentPage == index) 31.dp else 12.dp)
                        .height(10.dp)
                        .background(
                            if (pagerState.currentPage == index) PopupGreen else PopupGreen.copy(alpha = 0.22f),
                            RoundedCornerShape(2.dp),
                        ),
                )
            }
        }
        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PopupGreen),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Add my first link", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IdeaLinkSlide(onDismiss: () -> Unit) {
    val ideasBackground = Color(0xFFF1FFF8)

    Box(Modifier.fillMaxSize().background(ideasBackground)) {
        Image(
            painter = painterResource(R.drawable.ideas_popup),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.67f)
                .height(385.dp),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.67f)
                .height(385.dp)
                .background(
                    Brush.horizontalGradient(
                        0f to ideasBackground,
                        0.22f to ideasBackground.copy(alpha = 0.82f),
                        0.58f to Color.Transparent,
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.58f to Color.Transparent,
                        0.86f to ideasBackground,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 90.dp),
        ) {
            IconTile(
                icon = Icons.Filled.Link,
                tint = PopupGreen,
                fill = PopupGreen,
                modifier = Modifier.padding(start = 38.dp, top = 36.dp),
                size = 44.dp,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.Black)) { append("Your ideas\n") }
                    withStyle(SpanStyle(color = PopupGreen)) { append("in one place.") }
                },
                fontSize = 29.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 38.dp, end = 190.dp),
            )
            Text(
                "A home for every link you mean to come back to sorted for you, automatically",
                color = PopupBody,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(start = 38.dp, top = 20.dp, end = 190.dp),
            )
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .offset(y = (-28).dp)
                    .padding(start = 24.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ideas_icons),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(177.dp).height(158.dp),
                )
                Spacer(Modifier.height(8.dp))
                Column(Modifier.padding(start = 14.dp)) {
                    Text("Share a link from", color = Color.Black, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("any app", color = PopupGreen, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        CloseButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 14.dp),
        )
    }
}

@Composable
private fun IdeaOrganizedSlide(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Image(
            painter = painterResource(R.drawable.ideas_popup_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.55f)
                .height(395.dp),
        )
        Column(Modifier.padding(start = 38.dp, top = 54.dp, end = 205.dp)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.Black)) { append("Find it\n") }
                    withStyle(SpanStyle(color = PopupGreen)) { append("neatly\norganized") }
                },
                fontSize = 29.sp,
                lineHeight = 35.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                "Everything lands in the right place recipes, videos and articles, ready when you are.",
                color = PopupBody,
                fontSize = 17.sp,
                lineHeight = 25.sp,
                modifier = Modifier.padding(top = 34.dp),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 38.dp)
                .padding(top = 420.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryStat(Icons.Filled.Restaurant, "Recipes", "12")
            CategoryDivider()
            CategoryStat(Icons.Filled.PlayCircle, "Videos", "8")
            CategoryDivider()
            CategoryStat(Icons.Filled.Article, "Articles", "5")
        }
        CloseButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 14.dp),
        )
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFF4F4FA),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.size(34.dp),
        onClick = onClick,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF111827), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun IconTile(
    icon: ImageVector,
    tint: Color,
    fill: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 54.dp,
) {
    Surface(color = fill.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp), modifier = modifier.size(size)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.58f))
        }
    }
}

@Composable
private fun CategoryStat(icon: ImageVector, title: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        IconTile(icon = icon, tint = Color(0xFF4D9F6D), fill = Color(0xFF4D9F6D), size = 56.dp)
        Text(title, color = Color(0xFF242424), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(count, color = Color(0xFF4D9F6D), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CategoryDivider() {
    Spacer(Modifier.width(1.dp).height(120.dp).background(Color(0xFFEDEDED)))
}

private val PopupBlue = Color(0xFF00509D)
private val PopupRed = Color(0xFFD71920)
private val PopupGreen = Color(0xFF12BD6B)
private val PopupBody = Color(0xFF555555)
