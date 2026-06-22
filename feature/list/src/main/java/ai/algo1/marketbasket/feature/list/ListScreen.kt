package ai.algo1.marketbasket.feature.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors

@Composable
fun ListRoute(
    viewModel: ListViewModel = hiltViewModel(),
    searchOpen: Boolean = false,
    onSearchClose: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ListScreen(
        state = state,
        searchOpen = searchOpen,
        onSearchClose = onSearchClose,
        onAdd = viewModel::addByName,
        onToggle = viewModel::toggle,
        onDelete = viewModel::delete,
    )
}

@Composable
fun ListScreen(
    state: ListUiState,
    searchOpen: Boolean = false,
    onSearchClose: () -> Unit = {},
    onAdd: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var expandedId by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }

    // Reconcile expanded state with external (realtime) changes: clear a stale id if its item is gone.
    LaunchedEffect(state.categories) {
        if (expandedId != null && state.categories.none { c -> c.items.any { it.id == expandedId } }) {
            expandedId = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
        ListSaleBanner()

        if (searchOpen) {
            SearchAddRow(
                draft = draft,
                onDraftChange = { draft = it },
                onSubmit = {
                    onAdd(draft)
                    draft = ""
                },
                onSearchClose = {
                    draft = ""
                    onSearchClose()
                },
            )
        } else {
            ListHeaderStrip(state = state)
        }

        if (state.categories.isEmpty()) {
            EmptyListState()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 142.dp)) {
                state.categories.forEach { category ->
                    item(key = "h_${category.id}") {
                        Text(
                            text = category.name.uppercase(),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MarketBasketColors.Primary,
                        )
                    }
                    items(category.items, key = { it.id }) { groceryItem ->
                        ListItem(
                            item = groceryItem,
                            expanded = expandedId == groceryItem.id,
                            onClick = { expandedId = if (expandedId == groceryItem.id) null else groceryItem.id },
                            onToggle = { onToggle(groceryItem.id) },
                            onDelete = { onDelete(groceryItem.id); expandedId = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListSaleBanner() {
    Box(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 8.dp)) {
        Surface(
            color = Color(0xFFFFF4F4),
            border = BorderStroke(1.dp, Color(0xFFF8DDDD)),
            modifier = Modifier.fillMaxWidth()
                .shadow(10.dp, ambientColor = Color(0x0FD71920), spotColor = Color(0x0FD71920)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFFFFE1E4), RoundedCornerShape(14.dp)),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Color(0xFFD71920), modifier = Modifier.size(23.dp))
                }
                Column(Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("3 items in your list are on sale!", color = Color(0xFF080816), fontSize = 15.sp, lineHeight = 18.sp)
                    Text("Save ${'$'}6.47 today", color = Color(0xFFD71920), fontSize = 18.sp, lineHeight = 21.sp, fontWeight = FontWeight.Black)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF080816), modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun ListHeaderStrip(state: ListUiState) {
    val itemCount = state.categories.sumOf { it.items.size }
    val activeCategory = state.categories.firstOrNull()?.name ?: "Produce"
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp).padding(top = 2.dp, bottom = 12.dp),
    ) {
        Text(
            "My List ($itemCount)",
            color = Color(0xFF080816),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
        )
        Text(
            activeCategory.uppercase(),
            color = MarketBasketColors.Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun SearchAddRow(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSearchClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .shadow(18.dp, ambientColor = Color(0x14080816), spotColor = Color(0x14080816))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier.weight(1f).height(56.dp),
            singleLine = true,
            placeholder = { Text("Search items...", color = Color(0x66080816), fontSize = 17.sp) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0x1A080816),
                unfocusedContainerColor = Color(0x1A080816),
                disabledContainerColor = Color(0x1A080816),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(24.dp),
        )
        Surface(
            onClick = onSearchClose,
            shape = CircleShape,
            color = Color(0x1A080816),
            modifier = Modifier.size(56.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Icon(Icons.Filled.Close, contentDescription = "Close search", tint = Color(0xFF080816), modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun EmptyListState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 106.dp, bottom = 142.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.empty_list),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(112.dp),
        )
        Text(
            "Your list is empty",
            color = Color(0xFF18332D),
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            "Send items via WhatsApp or use search to add\ngroceries",
            color = Color(0xFF808080),
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        Spacer(Modifier.height(20.dp))
        Canvas(Modifier.width(64.dp).height(132.dp)) {
            drawLine(
                color = Color(0xFFCFCFCF),
                start = Offset(size.width * 0.38f, 0f),
                end = Offset(size.width * 0.72f, size.height * 0.34f),
                strokeWidth = 2f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f)),
            )
            drawLine(
                color = Color(0xFFCFCFCF),
                start = Offset(size.width * 0.72f, size.height * 0.34f),
                end = Offset(size.width * 0.32f, size.height * 0.68f),
                strokeWidth = 2f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f)),
            )
            drawLine(
                color = Color(0xFFCFCFCF),
                start = Offset(size.width * 0.32f, size.height * 0.68f),
                end = Offset(size.width * 0.72f, size.height),
                strokeWidth = 2f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f)),
            )
        }
    }
}
