package ai.algo1.marketbasket.feature.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors
import ai.algo1.marketbasket.core.domain.deals.Deal
import coil.compose.AsyncImage

private val DealRed = Color(0xFFC7353A)

@Composable
fun DealCard(deal: Deal, isAdded: Boolean, onAdd: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (deal.image != null) {
                AsyncImage(
                    model = deal.image,
                    contentDescription = deal.itemName,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(MarketBasketColors.ImagePlaceholder),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(deal.itemName.take(1).uppercase(), color = MarketBasketColors.TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                deal.itemName,
                fontWeight = FontWeight.Bold,
                color = MarketBasketColors.Primary,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (deal.description.isNotBlank()) {
                Text(deal.description, color = MarketBasketColors.TextSecondary, fontSize = 14.sp)
            }
            Text(
                displayPrice(deal.price),
                color = DealRed,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (deal.savings.isNotBlank()) {
                Text("Save ${deal.savings}", color = MarketBasketColors.TextMuted, fontSize = 12.sp)
            }
            if (isAdded) {
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Added")
                }
            } else {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = DealRed),
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text("Add to list") }
            }
        }
    }
}

/** Prepend "$" only when the price is a bare number (e.g. "3.99"); leave "2 for $3", "90¢" as-is. */
private fun displayPrice(price: String): String =
    if (price.matches(Regex("""\d+(\.\d+)?"""))) "${'$'}$price" else price
