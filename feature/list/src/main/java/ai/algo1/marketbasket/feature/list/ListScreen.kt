package ai.algo1.marketbasket.feature.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors
import ai.algo1.marketbasket.core.domain.model.CatalogProduct
import coil.compose.AsyncImage

private val SpringEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

@Composable
fun ListRoute(
    viewModel: ListViewModel = hiltViewModel(),
    searchOpen: Boolean = false,
    onSearchClose: () -> Unit = {},
    onItemsAdded: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ListScreen(
        state = state,
        searchOpen = searchOpen,
        onSearchClose = onSearchClose,
        onSearchQueryChange = viewModel::setSearchQuery,
        onAdd = { name ->
            viewModel.addByName(name)
            if (name.isNotBlank()) onItemsAdded()
        },
        onAddFromCatalog = { product ->
            viewModel.addFromCatalog(product)
            onItemsAdded()
        },
        onIncrement = viewModel::increment,
        onDecrement = viewModel::decrement,
        onToggle = viewModel::toggle,
        onDelete = viewModel::delete,
    )
}

@Composable
fun ListScreen(
    state: ListUiState,
    searchOpen: Boolean = false,
    onSearchClose: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onAdd: (String) -> Unit,
    onAddFromCatalog: (CatalogProduct) -> Unit = {},
    onIncrement: (String) -> Unit = {},
    onDecrement: (String) -> Unit = {},
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var expandedId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.categories) {
        if (expandedId != null && state.categories.none { c -> c.items.any { it.id == expandedId } }) {
            expandedId = null
        }
    }

    val alreadyAddedNames = remember(state.categories) {
        state.categories.flatMap { it.items }.map { it.name.lowercase().trim() }.toSet()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
        ListSaleBanner()

        AnimatedVisibility(
            visible = searchOpen,
            enter = slideInVertically(animationSpec = tween(260, easing = SpringEasing), initialOffsetY = { -it }) +
                fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(animationSpec = tween(180), targetOffsetY = { -it / 2 }) +
                fadeOut(animationSpec = tween(140)),
        ) {
            SearchAddRow(
                query = state.searchQuery,
                isAlreadyAdded = state.searchQuery.lowercase().trim() in alreadyAddedNames,
                onQueryChange = onSearchQueryChange,
                onSubmit = { onAdd(state.searchQuery) },
                onSearchClose = {
                    onSearchQueryChange("")
                    onSearchClose()
                },
            )
        }
        if (!searchOpen) {
            ListHeaderStrip(state = state)
        }

        when {
            searchOpen && state.searchQuery.isNotBlank() -> {
                if (state.isSearching) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MarketBasketColors.Primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 142.dp),
                    ) {
                        // Manual add row — typed query as top result
                        item(key = "manual_add") {
                            ManualAddResultItem(
                                query = state.searchQuery,
                                isAlreadyAdded = state.searchQuery.lowercase().trim() in alreadyAddedNames,
                                onAdd = { onAdd(state.searchQuery) },
                            )
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
                        }
                        items(state.searchResults, key = { it.urn }) { product ->
                            SearchResultItem(
                                product = product,
                                isAlreadyAdded = product.name.lowercase().trim() in alreadyAddedNames,
                                onAdd = { onAddFromCatalog(product) },
                            )
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F0)))
                        }
                    }
                }
            }
            state.categories.isEmpty() -> EmptyListState()
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 142.dp),
                ) {
                    state.categories.forEach { category ->
                        item(key = "h_${category.id}") {
                            Box(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F7F7))
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = category.name.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = MarketBasketColors.TextSecondary,
                                )
                            }
                        }
                        items(category.items, key = { it.id }) { groceryItem ->
                            ListItem(
                                item = groceryItem,
                                expanded = expandedId == groceryItem.id,
                                onClick = { expandedId = if (expandedId == groceryItem.id) null else groceryItem.id },
                                onToggle = { onToggle(groceryItem.id) },
                                onDelete = { onDelete(groceryItem.id); expandedId = null },
                                onIncrement = { onIncrement(groceryItem.id) },
                                onDecrement = { onDecrement(groceryItem.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualAddResultItem(query: String, isAlreadyAdded: Boolean, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                .background(MarketBasketColors.ImagePlaceholder),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                query.take(1).uppercase(),
                color = MarketBasketColors.TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }
        Text(
            text = query,
            modifier = Modifier.weight(1f),
            fontSize = 17.sp,
            color = MarketBasketColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        AddOrCheckButton(isAdded = isAlreadyAdded, onAdd = onAdd)
    }
}

@Composable
private fun SearchResultItem(product: CatalogProduct, isAlreadyAdded: Boolean, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (product.imageUrl != null) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit,
            )
        } else {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(MarketBasketColors.ImagePlaceholder),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    product.name.take(1).uppercase(),
                    color = MarketBasketColors.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
        }
        Text(
            text = product.name,
            modifier = Modifier.weight(1f),
            fontSize = 17.sp,
            color = MarketBasketColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        AddOrCheckButton(isAdded = isAlreadyAdded, onAdd = onAdd)
    }
}

@Composable
private fun AddOrCheckButton(isAdded: Boolean, onAdd: () -> Unit) {
    val bounceScale = remember { Animatable(1f) }
    var lastIsAdded by remember { mutableStateOf(isAdded) }

    LaunchedEffect(isAdded) {
        if (isAdded && !lastIsAdded) {
            bounceScale.snapTo(1f)
            bounceScale.animateTo(0.72f, tween(106, easing = SpringEasing))
            bounceScale.animateTo(1.22f, tween(114, easing = SpringEasing))
            bounceScale.animateTo(0.94f, tween(84, easing = SpringEasing))
            bounceScale.animateTo(1f, tween(76, easing = SpringEasing))
        }
        lastIsAdded = isAdded
    }

    if (isAdded) {
        Box(
            modifier = Modifier.size(32.dp).scale(bounceScale.value).clip(CircleShape)
                .background(MarketBasketColors.CheckboxChecked),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Added", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    } else {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAdd,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(32.dp)) {
                drawCircle(
                    color = Color(0xFFD0D0D0),
                    radius = size.minDimension / 2f - 1.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
            Icon(Icons.Filled.Add, contentDescription = "Add to list", tint = Color(0xFF080816), modifier = Modifier.size(18.dp))
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFFFFE1E4), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
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
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp)
            .padding(top = 2.dp, bottom = 12.dp),
    ) {
        Text(
            "My List ($itemCount)",
            color = Color(0xFF080816),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun SearchAddRow(
    query: String,
    isAlreadyAdded: Boolean,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSearchClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).height(56.dp),
            singleLine = true,
            placeholder = { Text("Search items...", color = Color(0x88080816), fontSize = 16.sp) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE3E3E3),
                unfocusedContainerColor = Color(0xFFE3E3E3),
                disabledContainerColor = Color(0xFFE3E3E3),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RectangleShape,
            trailingIcon = if (query.isNotBlank()) {
                {
                    if (isAlreadyAdded) {
                        Text(
                            "Added",
                            color = MarketBasketColors.CheckboxChecked.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    } else {
                        Text(
                            "Add to list",
                            color = MarketBasketColors.CheckboxChecked,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 12.dp).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSubmit,
                            ),
                        )
                    }
                }
            } else null,
        )
        Box(
            modifier = Modifier.size(56.dp).background(Color(0xFFE3E3E3)).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSearchClose,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Close search", tint = Color(0xFF080816), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun EmptyListState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 106.dp, bottom = 142.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
            drawLine(color = Color(0xFFCFCFCF), start = Offset(size.width * 0.38f, 0f), end = Offset(size.width * 0.72f, size.height * 0.34f), strokeWidth = 2f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f)))
            drawLine(color = Color(0xFFCFCFCF), start = Offset(size.width * 0.72f, size.height * 0.34f), end = Offset(size.width * 0.32f, size.height * 0.68f), strokeWidth = 2f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f)))
            drawLine(color = Color(0xFFCFCFCF), start = Offset(size.width * 0.32f, size.height * 0.68f), end = Offset(size.width * 0.72f, size.height), strokeWidth = 2f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f)))
        }
    }
}
