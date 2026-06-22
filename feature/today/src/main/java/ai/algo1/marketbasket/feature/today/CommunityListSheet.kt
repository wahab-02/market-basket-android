package ai.algo1.marketbasket.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.core.domain.today.CommunityList
import ai.algo1.marketbasket.core.domain.today.CommunityListItem
import ai.algo1.marketbasket.core.domain.util.ItemName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommunityListSheet(
    list: CommunityList,
    addedItemNames: Set<String>,
    onAddItems: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(62.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Text(list.icon, fontSize = 30.sp)
                }
                Column(Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(list.title, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = TodayColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(list.description, fontSize = 17.sp, color = TodayColors.Ink.copy(alpha = 0.68f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(Modifier.padding(top = 28.dp).fillMaxWidth().height(1.dp).background(Color(0xFFE8E8E8)))
            Row(Modifier.padding(top = 28.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${list.itemCount} items", fontSize = 18.sp, color = TodayColors.Ink)
                Text(
                    "Add all to list",
                    fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TodayColors.Blue,
                    modifier = Modifier.tap { onAddItems(list.items.map { it.name }) },
                )
            }
            LazyColumn(Modifier.padding(top = 20.dp).heightIn(min = 220.dp, max = 480.dp)) {
                items(list.items, key = { it.name }) { item ->
                    CommunityListRow(
                        item = item,
                        alreadyAdded = ItemName.normalize(item.name) in addedItemNames,
                        onAdd = { onAddItems(listOf(item.name)) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun CommunityListRow(item: CommunityListItem, alreadyAdded: Boolean, onAdd: () -> Unit) {
    var added by remember(item.name) { mutableStateOf(alreadyAdded) }
    Row(
        Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // V1 placeholder tile (live product image deferred to Plan 13)
        Box(Modifier.size(58.dp).clip(RoundedCornerShape(6.dp)).background(TodayColors.RowImageBg))
        Text(item.name, Modifier.padding(horizontal = 16.dp).weight(1f), fontSize = 19.sp, color = TodayColors.RowText, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Box(
            Modifier.size(46.dp).clip(CircleShape).background(if (added) TodayColors.AddedGreen else TodayColors.RowAddBg)
                .tap { if (!added) { added = true; onAdd() } },
            contentAlignment = Alignment.Center,
        ) {
            Text(if (added) "✓" else "+", fontSize = 20.sp, color = if (added) Color.White else TodayColors.RowAddInk)
        }
    }
}

/** Flat tap target without ripple or min-size, matching the web's flat taps. */
@Composable
private fun Modifier.tap(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}
