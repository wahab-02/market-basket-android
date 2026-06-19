package ai.algo1.marketbasket.feature.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors

@Composable
fun MarketBasketTopBar(greeting: String, onSearchToggle: () -> Unit) {
    Surface(color = MarketBasketColors.BackgroundWhite) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = greeting, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MarketBasketColors.TextPrimary)
            IconButton(onClick = onSearchToggle) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = MarketBasketColors.TextPrimary)
            }
        }
    }
}
