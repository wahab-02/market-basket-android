package ai.algo1.marketbasket.feature.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import coil.compose.AsyncImage

@Composable
fun ListItem(
    item: GroceryItem,
    expanded: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)),
            )
        } else {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MarketBasketColors.ImagePlaceholder),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.name.take(1).uppercase(), color = MarketBasketColors.TextSecondary, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            color = MarketBasketColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
        )

        if (expanded) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MarketBasketColors.TextSecondary)
            }
        } else {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(MarketBasketColors.BackgroundAlt),
                contentAlignment = Alignment.Center,
            ) {
                Text("${item.quantity ?: 1}", fontSize = 13.sp, color = MarketBasketColors.TextPrimary)
            }
        }

        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape)
                .background(if (item.checked) MarketBasketColors.CheckboxChecked else MarketBasketColors.BackgroundAlt)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            if (item.checked) {
                Icon(Icons.Filled.Check, contentDescription = "Checked", tint = MarketBasketColors.BackgroundWhite, modifier = Modifier.size(18.dp))
            }
        }
    }
}
