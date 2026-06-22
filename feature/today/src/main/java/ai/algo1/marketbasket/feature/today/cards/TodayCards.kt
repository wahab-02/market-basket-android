package ai.algo1.marketbasket.feature.today.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.core.domain.today.CommunityList
import ai.algo1.marketbasket.feature.today.R
import ai.algo1.marketbasket.feature.today.TodayColors
import ai.algo1.marketbasket.feature.today.TodayImages

private fun Modifier.todayCardShadow(): Modifier =
    shadow(18.dp, ambientColor = Color(0x14080816), spotColor = Color(0x14080816))

private fun Modifier.recipeCardShadow(): Modifier =
    shadow(16.dp, ambientColor = Color(0x12111116), spotColor = Color(0x12111116))

private fun Modifier.communityCardShadow(): Modifier =
    shadow(18.dp, RoundedCornerShape(9.dp), ambientColor = Color(0x1F080816), spotColor = Color(0x1F080816))

@Composable
internal fun ChevronRightIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = TodayColors.Chevron,
        modifier = modifier.size(21.dp),
    )
}

@Composable
internal fun TodayMetric(value: Int, label: String, highlight: Boolean = false, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value.toString(),
            fontSize = 29.sp, fontWeight = FontWeight.ExtraBold,
            color = if (highlight) TodayColors.Green else TodayColors.Ink,
        )
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.em, color = TodayColors.Ink.copy(alpha = 0.42f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun DealsSavingsCard(savingsTotal: String, saleCountLabel: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White,
        modifier = Modifier.fillMaxWidth().todayCardShadow(),
    ) {
        Box {
            Box(Modifier.fillMaxWidth().height(3.dp).background(TodayColors.Red)) // top border
            Column(Modifier.padding(20.dp).padding(end = 118.dp)) {
                Text(
                    "DEALS FOR YOUR LIST",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 0.28.em, color = TodayColors.Red, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    savingsTotal,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = TodayColors.Green,
                )
                Text(
                    "you can save today",
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TodayColors.Ink.copy(alpha = 0.62f),
                )
            }
            Surface(
                color = TodayColors.GreenPillBg,
                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            ) {
                Text(
                    saleCountLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TodayColors.Green,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            ChevronRightIcon(Modifier.align(Alignment.CenterEnd).padding(end = 20.dp))
        }
    }
}

@Composable
internal fun ListStatsCard(
    itemCount: Int, categoryCount: Int, saleCount: Int, tripMinutes: Int, onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = Color.White, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).todayCardShadow()) {
        Box {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "YOUR LIST",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 0.28.em, color = TodayColors.Ink.copy(alpha = 0.58f), maxLines = 1,
                )
                Row(Modifier.padding(top = 16.dp).fillMaxWidth().padding(end = 28.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    TodayMetric(itemCount, "Items", modifier = Modifier.weight(1f))
                    Box(Modifier.height(48.dp).width(1.dp).background(TodayColors.MetricDivider))
                    TodayMetric(categoryCount, "Categories", modifier = Modifier.weight(1.12f).padding(start = 12.dp))
                    Box(Modifier.height(48.dp).width(1.dp).background(TodayColors.MetricDivider))
                    TodayMetric(saleCount, "On sale", highlight = true, modifier = Modifier.weight(1f).padding(start = 12.dp))
                }
            }
            Surface(
                color = TodayColors.NeutralPillBg,
                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            ) {
                Text(
                    "$tripMinutes min trip",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TodayColors.Ink.copy(alpha = 0.64f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            ChevronRightIcon(Modifier.align(Alignment.CenterEnd).padding(end = 20.dp))
        }
    }
}

@Composable
internal fun TodayRecipeCard(onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.White, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).recipeCardShadow()) {
        Box {
            Box(Modifier.padding(start = 0.dp, top = 24.dp).height(22.dp).width(4.dp).background(TodayColors.SpGreen))
            Column(Modifier.padding(16.dp).padding(end = 26.dp)) {
                Row {
                    Image(
                        painter = painterResource(R.drawable.today_chicken_alfredo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(width = 48.dp, height = 47.dp).background(TodayColors.SpImageBg),
                    )
                    Column(Modifier.padding(start = 10.dp).weight(1f)) {
                        Text("Chicken Alfredo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TodayColors.SpTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("You have 8 of 10 ingredients.\n2 missing items are on sale.", fontSize = 12.sp, color = TodayColors.SpBody, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Box(Modifier.padding(top = 10.dp).fillMaxWidth().height(1.dp).background(TodayColors.SpHairline))
                Row(Modifier.padding(top = 10.dp)) {
                    Text("Chicken Alfredo - ", fontSize = 12.sp, color = TodayColors.SpBody)
                    Text("92% ready", fontSize = 12.sp, color = TodayColors.SpGreen)
                }
            }
            ChevronRightIcon(Modifier.align(Alignment.CenterEnd).padding(end = 20.dp))
        }
    }
}

@Composable
internal fun CommunitySuggestionCard(list: CommunityList, onShop: () -> Unit) {
    Surface(color = TodayColors.Ink, shape = RoundedCornerShape(9.dp), modifier = Modifier.fillMaxWidth().height(246.dp).communityCardShadow()) {
        Box {
            Image(
                painter = painterResource(TodayImages.community(list.cardImageKey)),
                contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(246.dp),
            )
            // bottom scrim
            Box(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().height(160.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, TodayColors.Ink.copy(alpha = 0.72f)))),
            )
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) { Text(list.icon, fontSize = 26.sp) }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(list.title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(list.description, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.88f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Surface(onClick = onShop, color = TodayColors.CommunityRed, shape = CircleShape, modifier = Modifier.height(34.dp).padding(start = 12.dp)) {
                    Box(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                        Text("Shop it", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CreateCommunityListCard() {
    Surface(color = TodayColors.CreateCardBg, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Want to create your own list?", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(
                    "Share your list with the community and inspire other shoppers.",
                    modifier = Modifier.padding(top = 16.dp), fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color.Black,
                    maxLines = 3, overflow = TextOverflow.Ellipsis,
                )
                Surface(color = TodayColors.CommunityRed, shape = CircleShape, modifier = Modifier.padding(top = 20.dp).height(42.dp).defaultMinSize(minWidth = 126.dp)) {
                    Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                        Text("Create a list", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
            }
            Image(
                painter = painterResource(R.drawable.community_createlist),
                contentDescription = null, contentScale = ContentScale.Fit,
                modifier = Modifier.weight(0.72f).widthIn(max = 172.dp),
            )
        }
    }
}
