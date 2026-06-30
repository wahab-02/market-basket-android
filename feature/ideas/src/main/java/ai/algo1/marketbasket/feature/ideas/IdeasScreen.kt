package ai.algo1.marketbasket.feature.ideas

import android.content.Intent
import android.view.ViewGroup
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

private val VideoAccentColor = Color(0xFFC7353A)
private val OverlayEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private const val CollapsedDescriptionLength = 85
private const val TabBarScrimAlpha = 0.42f

private val IdeasTabTitles = listOf("Videos", "Recipes", "Meal Planning")

@Composable
fun IdeasRoute(
    viewModel: IdeasViewModel = hiltViewModel(),
    onTabChange: (Int) -> Unit = {},
    onItemsAdded: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    IdeasScreen(
        state = state,
        player = viewModel.player,
        onPageSettled = viewModel::onPageSettled,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onBookmarkToggle = viewModel::onBookmarkToggle,
        onAddToListPress = viewModel::onAddToListPress,
        onAddToListDismiss = viewModel::onAddToListDismiss,
        onAddIngredientsConfirmed = viewModel::onAddIngredientsConfirmed,
        onAddRecipeIngredient = viewModel::onAddRecipeIngredient,
        onAddMissingRecipeIngredients = viewModel::onAddMissingRecipeIngredients,
        onTabChange = onTabChange,
        onItemsAdded = onItemsAdded,
    )
}

@Composable
fun IdeasScreen(
    state: IdeasUiState,
    player: ExoPlayer,
    onPageSettled: (Int) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onBookmarkToggle: (String) -> Unit,
    onAddToListPress: (Recipe) -> Unit,
    onAddToListDismiss: () -> Unit,
    onAddIngredientsConfirmed: (String) -> Unit,
    onAddRecipeIngredient: (String, RecipeIngredient) -> Unit,
    onAddMissingRecipeIngredients: (String) -> Unit,
    onTabChange: (Int) -> Unit = {},
    onItemsAdded: (String) -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isVideoExpanded by remember { mutableStateOf(false) }
    var selectedRecipeId by remember { mutableStateOf<String?>(null) }
    var pendingVideoAddNotification by remember { mutableStateOf(false) }
    val recipesWithShoppingList = remember(state.shoppingCategories) {
        recipeCards.map { it.withShoppingListMatches(state.shoppingCategories) }
    }
    val selectedRecipe = selectedRecipeId?.let { id ->
        recipesWithShoppingList.firstOrNull { it.id == id }
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab != 0) isVideoExpanded = false
        selectedRecipeId = null
        onTabChange(selectedTab)
    }
    BackHandler(enabled = selectedRecipeId != null) {
        selectedRecipeId = null
    }
    LaunchedEffect(state.selectedRecipeForList, state.isAddingToList, pendingVideoAddNotification) {
        if (pendingVideoAddNotification && state.selectedRecipeForList == null && !state.isAddingToList) {
            pendingVideoAddNotification = false
            onItemsAdded("Items added from video")
        }
    }

    val view = LocalView.current
    val isVideosTab = selectedTab == 0
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = !isVideosTab
        controller.isAppearanceLightNavigationBars = !isVideosTab
    }

    val contentBackground = if (selectedTab == 0) Color.Black else Color(0xFFF0F0F0)
    Box(Modifier.fillMaxSize().background(contentBackground)) {
        if (selectedRecipe != null) {
            RecipeDetailScreen(
                recipe = selectedRecipe,
                onBack = { selectedRecipeId = null },
                onAddIngredient = { ingredient ->
                    onAddRecipeIngredient(selectedRecipe.id, ingredient)
                    onItemsAdded("Items added from recipe")
                },
                onAddMissingIngredients = {
                    onAddMissingRecipeIngredients(selectedRecipe.id)
                    onItemsAdded("Items added from recipe")
                },
            )
        } else {
            when (selectedTab) {
                0 -> VideosTab(
                    state = state,
                    player = player,
                    onPageSettled = {
                        isVideoExpanded = false
                        onPageSettled(it)
                    },
                    onFavoriteToggle = onFavoriteToggle,
                    onBookmarkToggle = onBookmarkToggle,
                    onAddToListPress = onAddToListPress,
                    onExpandedChange = { isVideoExpanded = it },
                )
                1 -> RecipesTab(
                    recipes = recipesWithShoppingList,
                    onViewRecipe = { selectedRecipeId = it.id },
                    onAddMissingIngredients = {
                        onAddMissingRecipeIngredients(it.id)
                        onItemsAdded("Items added from recipe")
                    },
                )
                2 -> MealPlanningTab(onNavigateToRecipes = { selectedTab = 1 })
                else -> PlaceholderTab(IdeasTabTitles[selectedTab])
            }
        }

        if (selectedRecipeId == null) {
            IdeasTabBar(
                tabs = IdeasTabTitles,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                isVideoExpanded = isVideoExpanded,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        state.selectedRecipeForList?.let { recipe ->
            AddIngredientsDialog(
                recipe = recipe,
                isAdding = state.isAddingToList,
                onDismiss = onAddToListDismiss,
                onConfirm = {
                    pendingVideoAddNotification = true
                    onAddIngredientsConfirmed(recipe.id)
                },
            )
        }
    }
}

@Composable
private fun IdeasTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    isVideoExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    val isVideosTab = selectedIndex == 0
    val bgStartAlpha by animateFloatAsState(
        targetValue = if (isVideoExpanded) 0.98f else TabBarScrimAlpha,
        animationSpec = tween(300, easing = OverlayEasing),
        label = "tab_bar_bg_alpha",
    )
    val columnModifier = if (isVideosTab) {
        modifier.fillMaxWidth().background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to Color.Black.copy(alpha = bgStartAlpha),
                    0.78f to Color.Black.copy(alpha = bgStartAlpha),
                    1f to Color.Transparent,
                ),
            ),
        )
    } else {
        modifier.fillMaxWidth().background(Color.White)
    }
    Column(modifier = columnModifier) {
        Spacer(Modifier.statusBarsPadding())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                val textColor = if (isVideosTab) {
                    if (selected) Color.White else Color.White.copy(alpha = 0.5f)
                } else {
                    if (selected) Color(0xFF111111) else Color(0xFF111111).copy(alpha = 0.4f)
                }
                val indicatorColor = if (isVideosTab) Color.White else Color(0xFF111111)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) },
                        )
                        .padding(bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 18.dp),
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(2.dp)
                                .background(indicatorColor, RoundedCornerShape(1.dp)),
                        )
                    } else {
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VideosTab(
    state: IdeasUiState,
    player: ExoPlayer,
    onPageSettled: (Int) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onBookmarkToggle: (String) -> Unit,
    onAddToListPress: (Recipe) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { recipeVideos.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            onPageSettled(page)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1,
    ) { page ->
        val item = recipeVideos[page]
        val isCurrentPage = pagerState.settledPage == page
        val actions = state.videoActions[item.id] ?: VideoActionsState()

        VideoPage(
            item = item,
            isCurrentPage = isCurrentPage,
            player = player,
            actions = actions,
            onFavoriteToggle = { onFavoriteToggle(item.id) },
            onBookmarkToggle = { onBookmarkToggle(item.id) },
            onAddToListPress = { onAddToListPress(item.recipe) },
            onExpandedChange = onExpandedChange,
            recipe = item.recipe,
        )
    }
}

@Composable
private fun VideoPage(
    item: RecipeVideo,
    isCurrentPage: Boolean,
    player: ExoPlayer,
    actions: VideoActionsState,
    onFavoriteToggle: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onAddToListPress: () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    recipe: Recipe,
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            update = { playerView ->
                playerView.player = if (isCurrentPage) player else null
            },
            modifier = Modifier.fillMaxSize(),
        )

        // bottom scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                    ),
                ),
        )

        RecipeInfoOverlay(
            recipe = item.recipe,
            isExpanded = isExpanded,
            onExpandedChange = {
                isExpanded = it
                onExpandedChange(it)
            },
            modifier = Modifier.fillMaxSize(),
        )

        VideoActionButtons(
            actions = actions,
            onFavoriteToggle = onFavoriteToggle,
            onBookmarkToggle = onBookmarkToggle,
            onAddToListPress = onAddToListPress,
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, recipe.title)
                    putExtra(Intent.EXTRA_TEXT, recipe.description)
                }
                context.startActivity(Intent.createChooser(intent, null))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 120.dp),
        )
    }
}

@Composable
private fun RecipeInfoOverlay(
    recipe: Recipe,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 0.52f else 0f,
        animationSpec = tween(300, easing = OverlayEasing),
        label = "overlay_alpha",
    )
    val truncatedDescription = remember(recipe.description) {
        if (recipe.description.length <= CollapsedDescriptionLength) {
            recipe.description
        } else {
            val truncated = recipe.description.take(CollapsedDescriptionLength)
            val lastSpace = truncated.lastIndexOf(' ')
            if (lastSpace > 0) truncated.substring(0, lastSpace) else truncated
        }
    }
    val shouldShowSeeMore = recipe.description.length > CollapsedDescriptionLength

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onExpandedChange(!isExpanded) },
        ),
    ) {
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha)),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 56.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 160.dp),
            ) {
                Text(
                    text = recipe.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 34.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "by @${recipe.creator}",
                    color = VideoAccentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = recipe.description,
                    color = Color.White,
                    fontSize = 19.sp,
                    lineHeight = 25.sp,
                )
                Spacer(Modifier.height(20.dp))

                // time/servings/difficulty chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(recipe.time, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("${recipe.servings} servings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(recipe.difficulty, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(24.dp))
                Text("Ingredients", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                recipe.ingredients.forEach { ingredient ->
                    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(VideoAccentColor, CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(ingredient, color = Color.White, fontSize = 18.sp, lineHeight = 24.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Instructions", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                recipe.instructions.forEachIndexed { index, instruction ->
                    Row(modifier = Modifier.padding(bottom = 12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(VideoAccentColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(instruction, color = Color.White, fontSize = 18.sp, lineHeight = 24.sp)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 96.dp, bottom = 120.dp),
            ) {
                Text(
                    text = recipe.title,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                if (shouldShowSeeMore) {
                    Text(
                        text = buildAnnotatedString {
                            append(truncatedDescription)
                            append("... ")
                            withStyle(SpanStyle(color = VideoAccentColor, fontWeight = FontWeight.SemiBold)) {
                                append("See more")
                            }
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    )
                } else {
                    Text(
                        text = recipe.description,
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoActionButtons(
    actions: VideoActionsState,
    onFavoriteToggle: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onAddToListPress: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActionIconButton(onClick = onFavoriteToggle, contentDescription = "Favorite recipe") {
            HeartIcon(active = actions.favorited)
        }
        ActionIconButton(onClick = onBookmarkToggle, contentDescription = "Bookmark recipe") {
            BookmarkIcon(active = actions.bookmarked)
        }
        ActionIconButton(onClick = onAddToListPress, contentDescription = "Add ingredients to list") {
            AddToListIcon(active = actions.addedToList)
        }
        ActionIconButton(onClick = onShare, contentDescription = "Share recipe") {
            ShareIcon()
        }
    }
}

@Composable
private fun ActionIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "icon_press_scale",
    )
    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun HeartIcon(active: Boolean) {
    val fill = if (active) VideoAccentColor else Color.White
    Canvas(modifier = Modifier.size(32.dp)) {
        val sx = size.width / 24f
        val sy = size.height / 24f
        // Material Design filled heart: M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5
        // 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3
        // 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z
        val path = Path().apply {
            moveTo(12f * sx, 21.35f * sy)
            lineTo(10.55f * sx, 20.03f * sy)
            cubicTo(5.4f * sx, 15.36f * sy, 2f * sx, 12.28f * sy, 2f * sx, 8.5f * sy)
            cubicTo(2f * sx, 5.42f * sy, 4.42f * sx, 3f * sy, 7.5f * sx, 3f * sy)
            cubicTo(9.24f * sx, 3f * sy, 10.91f * sx, 3.81f * sy, 12f * sx, 5.09f * sy)
            cubicTo(13.09f * sx, 3.81f * sy, 14.76f * sx, 3f * sy, 16.5f * sx, 3f * sy)
            cubicTo(19.58f * sx, 3f * sy, 22f * sx, 5.42f * sy, 22f * sx, 8.5f * sy)
            cubicTo(22f * sx, 12.28f * sy, 18.6f * sx, 15.36f * sy, 13.45f * sx, 20.03f * sy)
            lineTo(12f * sx, 21.35f * sy)
            close()
        }
        drawPath(path, fill)
    }
}

@Composable
private fun BookmarkIcon(active: Boolean) {
    val fill = if (active) VideoAccentColor else Color.White
    Canvas(modifier = Modifier.size(32.dp)) {
        val path = Path().apply {
            val sx = size.width / 24f
            val sy = size.height / 24f
            moveTo(19f * sx, 21f * sy)
            lineTo(12f * sx, 16f * sy)
            lineTo(5f * sx, 21f * sy)
            lineTo(5f * sx, 5f * sy)
            cubicTo(5f * sx, 3.9f * sy, 5.9f * sx, 3f * sy, 7f * sx, 3f * sy)
            lineTo(17f * sx, 3f * sy)
            cubicTo(18.1f * sx, 3f * sy, 19f * sx, 3.9f * sy, 19f * sx, 5f * sy)
            close()
        }
        drawPath(path, fill)
    }
}

@Composable
private fun AddToListIcon(active: Boolean) {
    val fill = if (active) VideoAccentColor else Color.White
    Canvas(modifier = Modifier.size(32.dp)) {
        val path = Path().apply {
            val scaleX = size.width / 400f
            val scaleY = size.height / 352f
            // dot 1
            moveTo(0f * scaleX, 48f * scaleY)
            cubicTo(0f * scaleX, 60.73f * scaleY, 5.06f * scaleX, 72.94f * scaleY, 14.06f * scaleX, 81.94f * scaleY)
            cubicTo(23.06f * scaleX, 90.94f * scaleY, 35.27f * scaleX, 96f * scaleY, 48f * scaleX, 96f * scaleY)
            cubicTo(60.73f * scaleX, 96f * scaleY, 72.94f * scaleX, 90.94f * scaleY, 81.94f * scaleX, 81.94f * scaleY)
            cubicTo(90.94f * scaleX, 72.94f * scaleY, 96f * scaleX, 60.73f * scaleY, 96f * scaleX, 48f * scaleY)
            cubicTo(96f * scaleX, 35.27f * scaleY, 90.94f * scaleX, 23.06f * scaleY, 81.94f * scaleX, 14.06f * scaleY)
            cubicTo(72.94f * scaleX, 5.06f * scaleY, 60.73f * scaleX, 0f * scaleY, 48f * scaleX, 0f * scaleY)
            cubicTo(35.27f * scaleX, 0f * scaleY, 23.06f * scaleX, 5.06f * scaleY, 14.06f * scaleX, 14.06f * scaleY)
            cubicTo(5.06f * scaleX, 23.06f * scaleY, 0f * scaleX, 35.27f * scaleY, 0f * scaleX, 48f * scaleY)
            close()
            // line 1
            moveTo(144f * scaleX, 48f * scaleY)
            cubicTo(144f * scaleX, 30.33f * scaleY, 158.33f * scaleX, 16f * scaleY, 176f * scaleX, 16f * scaleY)
            lineTo(368f * scaleX, 16f * scaleY)
            cubicTo(385.67f * scaleX, 16f * scaleY, 400f * scaleX, 30.33f * scaleY, 400f * scaleX, 48f * scaleY)
            cubicTo(400f * scaleX, 65.67f * scaleY, 385.67f * scaleX, 80f * scaleY, 368f * scaleX, 80f * scaleY)
            lineTo(176f * scaleX, 80f * scaleY)
            cubicTo(158.33f * scaleX, 80f * scaleY, 144f * scaleX, 65.67f * scaleY, 144f * scaleX, 48f * scaleY)
            close()
            // dot 2
            moveTo(0f * scaleX, 176f * scaleY)
            cubicTo(0f * scaleX, 188.73f * scaleY, 5.06f * scaleX, 200.94f * scaleY, 14.06f * scaleX, 209.94f * scaleY)
            cubicTo(23.06f * scaleX, 218.94f * scaleY, 35.27f * scaleX, 224f * scaleY, 48f * scaleX, 224f * scaleY)
            cubicTo(60.73f * scaleX, 224f * scaleY, 72.94f * scaleX, 218.94f * scaleY, 81.94f * scaleX, 209.94f * scaleY)
            cubicTo(90.94f * scaleX, 200.94f * scaleY, 96f * scaleX, 188.73f * scaleY, 96f * scaleX, 176f * scaleY)
            cubicTo(96f * scaleX, 163.27f * scaleY, 90.94f * scaleX, 151.06f * scaleY, 81.94f * scaleX, 142.06f * scaleY)
            cubicTo(72.94f * scaleX, 133.06f * scaleY, 60.73f * scaleX, 128f * scaleY, 48f * scaleX, 128f * scaleY)
            cubicTo(35.27f * scaleX, 128f * scaleY, 23.06f * scaleX, 133.06f * scaleY, 14.06f * scaleX, 142.06f * scaleY)
            cubicTo(5.06f * scaleX, 151.06f * scaleY, 0f * scaleX, 163.27f * scaleY, 0f * scaleX, 176f * scaleY)
            close()
            // line 2
            moveTo(144f * scaleX, 176f * scaleY)
            cubicTo(144f * scaleX, 158.33f * scaleY, 158.33f * scaleX, 144f * scaleY, 176f * scaleX, 144f * scaleY)
            lineTo(368f * scaleX, 144f * scaleY)
            cubicTo(385.67f * scaleX, 144f * scaleY, 400f * scaleX, 158.33f * scaleY, 400f * scaleX, 176f * scaleY)
            cubicTo(400f * scaleX, 193.67f * scaleY, 385.67f * scaleX, 208f * scaleY, 368f * scaleX, 208f * scaleY)
            lineTo(176f * scaleX, 208f * scaleY)
            cubicTo(158.33f * scaleX, 208f * scaleY, 144f * scaleX, 193.67f * scaleY, 144f * scaleX, 176f * scaleY)
            close()
            // dot 3
            moveTo(0f * scaleX, 304f * scaleY)
            cubicTo(0f * scaleX, 316.73f * scaleY, 5.06f * scaleX, 328.94f * scaleY, 14.06f * scaleX, 337.94f * scaleY)
            cubicTo(23.06f * scaleX, 346.94f * scaleY, 35.27f * scaleX, 352f * scaleY, 48f * scaleX, 352f * scaleY)
            cubicTo(60.73f * scaleX, 352f * scaleY, 72.94f * scaleX, 346.94f * scaleY, 81.94f * scaleX, 337.94f * scaleY)
            cubicTo(90.94f * scaleX, 328.94f * scaleY, 96f * scaleX, 316.73f * scaleY, 96f * scaleX, 304f * scaleY)
            cubicTo(96f * scaleX, 291.27f * scaleY, 90.94f * scaleX, 279.06f * scaleY, 81.94f * scaleX, 270.06f * scaleY)
            cubicTo(72.94f * scaleX, 261.06f * scaleY, 60.73f * scaleX, 256f * scaleY, 48f * scaleX, 256f * scaleY)
            cubicTo(35.27f * scaleX, 256f * scaleY, 23.06f * scaleX, 261.06f * scaleY, 14.06f * scaleX, 270.06f * scaleY)
            cubicTo(5.06f * scaleX, 279.06f * scaleY, 0f * scaleX, 291.27f * scaleY, 0f * scaleX, 304f * scaleY)
            close()
            // line 3
            moveTo(144f * scaleX, 304f * scaleY)
            cubicTo(144f * scaleX, 286.33f * scaleY, 158.33f * scaleX, 272f * scaleY, 176f * scaleX, 272f * scaleY)
            lineTo(368f * scaleX, 272f * scaleY)
            cubicTo(385.67f * scaleX, 272f * scaleY, 400f * scaleX, 286.33f * scaleY, 400f * scaleX, 304f * scaleY)
            cubicTo(400f * scaleX, 321.67f * scaleY, 385.67f * scaleX, 336f * scaleY, 368f * scaleX, 336f * scaleY)
            lineTo(176f * scaleX, 336f * scaleY)
            cubicTo(158.33f * scaleX, 336f * scaleY, 144f * scaleX, 321.67f * scaleY, 144f * scaleX, 336f * scaleY)
            close()
        }
        drawPath(path, fill)
    }
}

@Composable
private fun ShareIcon() {
    Canvas(modifier = Modifier.size(32.dp)) {
        val sw = size.width / 24f
        val sh = size.height / 24f
        val paint = Stroke(width = 2f * sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // box: M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8
        val boxPath = Path().apply {
            moveTo(4f * sw, 12f * sh)
            lineTo(4f * sw, 20f * sh)
            cubicTo(4f * sw, 21.1f * sh, 4.9f * sw, 22f * sh, 6f * sw, 22f * sh)
            lineTo(18f * sw, 22f * sh)
            cubicTo(19.1f * sw, 22f * sh, 20f * sw, 21.1f * sh, 20f * sw, 20f * sh)
            lineTo(20f * sw, 12f * sh)
        }
        drawPath(boxPath, Color.White, style = paint)
        // arrow: M16 6l-4-4-4 4  +  M12 2v13
        val arrowPath = Path().apply {
            moveTo(16f * sw, 6f * sh)
            lineTo(12f * sw, 2f * sh)
            lineTo(8f * sw, 6f * sh)
            moveTo(12f * sw, 2f * sh)
            lineTo(12f * sw, 15f * sh)
        }
        drawPath(arrowPath, Color.White, style = paint)
    }
}

@Composable
private fun AddIngredientsDialog(
    recipe: Recipe,
    isAdding: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0A0A0A))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                // header
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "Add to shopping list",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = recipe.title,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // ingredient list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                ) {
                    recipe.ingredients.forEachIndexed { index, ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(VideoAccentColor, CircleShape),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = ingredient,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            )
                        }
                        if (index < recipe.ingredients.lastIndex) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Surface(
                        onClick = { if (!isAdding) onConfirm() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = VideoAccentColor,
                    ) {
                        Text(
                            text = if (isAdding) "Adding..." else "Add ingredients",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderTab(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
    }
}
