package ai.algo1.marketbasket.feature.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.core.domain.deals.Deal
import coil.compose.AsyncImage

private val DealRed = Color(0xFFC7353A)
private val DealBlue = Color(0xFF1E56A8)

@Composable
fun DealCard(deal: Deal, isAdded: Boolean, onAdd: () -> Unit) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)
            .shadow(8.dp, ambientColor = Color(0x0F080816), spotColor = Color(0x0F080816)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(260.dp).padding(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (deal.image != null) {
                    AsyncImage(
                        model = deal.image,
                        contentDescription = deal.itemName,
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Box(
                        modifier = Modifier.size(192.dp).clip(RoundedCornerShape(96.dp)).background(Color(0xFFF3F3F3)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(deal.itemName.take(1).uppercase(), color = DealBlue.copy(alpha = 0.2f), fontWeight = FontWeight.Bold, fontSize = 56.sp)
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    deal.itemName,
                    fontWeight = FontWeight.Bold,
                    color = DealBlue,
                    fontSize = 27.sp,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center,
                )
                if (deal.description.isNotBlank()) {
                    Text(
                        deal.description,
                        color = Color(0xFF3D3D3D),
                        fontSize = 23.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                Text(
                    displayPrice(deal.price),
                    color = DealRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 28.dp),
                )
                if (deal.savings.isNotBlank()) {
                    Text(
                        "SAVE ${deal.savings}",
                        color = DealRed.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp).padding(top = 28.dp, bottom = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = { if (!isAdded) onAdd() },
                    color = if (isAdded) Color.White else DealRed,
                    border = if (isAdded) BorderStroke(1.dp, DealRed) else null,
                    modifier = Modifier.weight(1f).height(53.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isAdded) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = DealRed, modifier = Modifier.size(22.dp))
                        }
                        Text(
                            if (isAdded) "Added" else "Add to list",
                            color = if (isAdded) DealRed else Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = if (isAdded) 8.dp else 0.dp),
                        )
                    }
                }
                Surface(
                    onClick = {},
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, DealRed.copy(alpha = 0.72f)),
                    modifier = Modifier.padding(start = 12.dp).size(width = 92.dp, height = 53.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Share, contentDescription = "Share deal", tint = DealRed, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
    }
}

/** Prepend "$" only when the price is a bare number (e.g. "3.99"); leave "2 for $3", "90¢" as-is. */
private fun displayPrice(price: String): String =
    if (price.matches(Regex("""\d+(\.\d+)?"""))) "${'$'}$price" else price
