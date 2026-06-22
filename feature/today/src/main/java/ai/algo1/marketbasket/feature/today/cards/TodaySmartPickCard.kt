package ai.algo1.marketbasket.feature.today.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.core.domain.today.SmartPickAction
import ai.algo1.marketbasket.core.domain.today.SmartPickCard
import ai.algo1.marketbasket.core.domain.today.SmartPickColor
import ai.algo1.marketbasket.feature.today.TodayColors
import ai.algo1.marketbasket.feature.today.TodayImages

private fun spColor(c: SmartPickColor): Color = when (c) {
    SmartPickColor.Red -> TodayColors.SpRed
    SmartPickColor.Green -> TodayColors.SpGreen
    SmartPickColor.Blue -> TodayColors.SpBlue
}

@Composable
internal fun TodaySmartPickCard(card: SmartPickCard, onAddToList: () -> Unit, onViewRecipe: () -> Unit) {
    var justAdded by remember { mutableStateOf(false) }
    val tagColor = spColor(card.tagColor)
    Surface(color = Color.White, modifier = Modifier.width(280.dp).height(380.dp)) {
        Column {
            Box(Modifier.fillMaxWidth().height(200.dp).background(TodayColors.SpImageBg)) {
                Image(
                    painter = painterResource(TodayImages.smartPick(card.imageKey)),
                    contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(200.dp),
                )
                card.topBadge?.let {
                    Surface(color = TodayColors.SpGreen, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                        Text(it, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                card.discountBadge?.let {
                    Surface(color = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                        Text(it, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TodayColors.SpRed)
                    }
                }
                Surface(color = if (card.tagColor == SmartPickColor.Red && card.tag != "From your ideas") tagColor else Color.White, modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                    Text(
                        card.tag.uppercase(),
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = if (card.tagColor == SmartPickColor.Red && card.tag != "From your ideas") Color.White else tagColor,
                    )
                }
            }
            Column(Modifier.padding(20.dp).weight(1f)) {
                Text(card.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TodayColors.SpTitle, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(card.description, Modifier.padding(top = 8.dp), fontSize = 13.sp, color = TodayColors.SpBody, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Box(Modifier.padding(top = 12.dp).height(34.dp)) {
                    val comp = card.comparison
                    val prog = card.progress
                    if (comp != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(comp.fromLabel.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TodayColors.SpMuted)
                                Text(comp.fromPrice, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TodayColors.SpTitle, textDecoration = TextDecoration.LineThrough)
                            }
                            Text("→", Modifier.padding(horizontal = 12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TodayColors.SpMuted)
                            Column {
                                Text(comp.toLabel.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TodayColors.SpGreen)
                                Text(comp.toPrice, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TodayColors.SpGreen)
                            }
                        }
                    } else if (prog != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f).height(3.dp).background(TodayColors.SpHairline)) {
                                Box(Modifier.fillMaxWidth(prog.percent / 100f).height(3.dp).background(spColor(prog.color)))
                            }
                            Text(prog.label, Modifier.padding(start = 8.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TodayColors.SpGreen)
                        }
                    }
                }
                Box(Modifier.padding(top = 16.dp).fillMaxWidth().height(1.dp).background(TodayColors.SpHairline))
                Row(Modifier.height(40.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    val label = if (card.action == SmartPickAction.AddToList && justAdded) "Added" else card.cta
                    Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TodayColors.SpRed, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false))
                    Text("→", fontSize = 14.sp, color = TodayColors.SpRed)
                }
            }
        }
    }
}
