package ai.algo1.marketbasket.feature.ideas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RecipesPageBg = Color(0xFFF0F0F0)
private val RecipesBrandRed = Color(0xFFB83040)
private val RecipesHeaderBg = Color(0xFFE4E4E4)
private val RecipesTextGray = Color(0xFF888888)
private val RecipesTextDark = Color(0xFF111111)
private val RecipesTagBorder = Color(0xFFCCCCCC)
private val RecipesMissingBorder = Color(0xFFDDDDDD)
private val RecipesCardBg = Color.White

@Composable
internal fun RecipesTab(
    recipes: List<RecipeCardItem> = recipeCards,
    onViewRecipe: (RecipeCardItem) -> Unit = {},
    onAddMissingIngredients: (RecipeCardItem) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RecipesPageBg),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Spacer(Modifier.statusBarsPadding().height(80.dp))
            }
            item {
                LinksBanner(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 4.dp, bottom = 4.dp),
                )
            }
            item {
                FilterBar(modifier = Modifier.padding(vertical = 8.dp))
            }
            item { Spacer(Modifier.height(8.dp)) }
            items(recipes, key = { it.id }) { card ->
                var bookmarked by remember { mutableStateOf(card.isBookmarked) }
                RecipeCard(
                    card = card,
                    isBookmarked = bookmarked,
                    onBookmarkToggle = { bookmarked = !bookmarked },
                    onViewRecipe = { onViewRecipe(card) },
                    onAddMissingIngredients = { onAddMissingIngredients(card) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            item {
                Spacer(Modifier.navigationBarsPadding().height(120.dp))
            }
        }

    }
}

@Composable
private fun LinksBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = RecipesCardBg,
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 4.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF4CAF50)),
            )
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ideas_link),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "All your links, organized",
                        color = RecipesTextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Share from any app, in one place",
                        color = RecipesTextGray,
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Image(
                        painter = painterResource(R.drawable.ideas_socials),
                        contentDescription = null,
                        modifier = Modifier.height(52.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add link",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterBar(modifier: Modifier = Modifier) {
    var selectedFilters by remember { mutableStateOf(emptySet<String>()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = {},
            shape = RoundedCornerShape(0.dp),
            color = RecipesBrandRed,
            modifier = Modifier.height(40.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Filters",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        listOf(
            ">80% Match" to Icons.Filled.FlashOn,
            "Under 30 mins" to null,
            "High Match" to null,
            "Vegan" to null,
            "Quick & Easy" to null,
        ).forEach { (label, icon) ->
            val selected = label in selectedFilters
            FilterChip(
                label = label,
                leadingIcon = icon,
                selected = selected,
                onClick = {
                    selectedFilters = if (selected) selectedFilters - label else selectedFilters + label
                },
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        color = if (selected) RecipesBrandRed else RecipesCardBg,
        shadowElevation = if (selected) 0.dp else 2.dp,
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE7E7E7)),
        modifier = Modifier.height(40.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (selected) Color.White else RecipesTextDark,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = label,
                color = if (selected) Color.White else RecipesTextDark.copy(alpha = 0.72f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun RecipeCard(
    card: RecipeCardItem,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onViewRecipe: () -> Unit = {},
    onAddMissingIngredients: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = RecipesCardBg,
        shape = RoundedCornerShape(2.dp),
    ) {
        Column {
            RecipeCardHeader(
                matchPercent = card.matchPercent,
                title = card.headerTitle,
                isBookmarked = isBookmarked,
                onBookmarkToggle = onBookmarkToggle,
            )

            Image(
                painter = painterResource(card.imageRes),
                contentDescription = card.fullTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 20.dp),
            ) {
                Text(
                    text = card.fullTitle,
                    color = RecipesTextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                )

                Spacer(Modifier.height(16.dp))

                RecipeStatsRow(
                    timeMinutes = card.timeMinutes,
                    calories = card.calories,
                    difficulty = card.difficulty,
                )

                Spacer(Modifier.height(14.dp))

                RecipeTagsRow(tags = card.tags)

                Spacer(Modifier.height(14.dp))
                if (card.missingIngredients.isNotEmpty()) {
                    MissingIngredientsBox(ingredients = card.missingIngredients)
                } else {
                    AllIngredientsBox()
                }

                Spacer(Modifier.height(16.dp))

                val allIngredientsAdded = card.missingIngredients.isEmpty()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        onClick = onViewRecipe,
                        modifier = Modifier
                            .weight(3f)
                            .height(48.dp),
                        color = RecipesBrandRed,
                        shape = RoundedCornerShape(2.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "View Recipe",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Surface(
                        onClick = { if (!allIngredientsAdded) onAddMissingIngredients() },
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(2.dp),
                        border = BorderStroke(
                            1.5.dp,
                            if (allIngredientsAdded) Color(0xFFDDDDDD) else RecipesBrandRed,
                        ),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (allIngredientsAdded) "+ Added" else "+ Missing",
                                color = if (allIngredientsAdded) Color(0xFFAAAAAA) else RecipesBrandRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCardHeader(
    matchPercent: Int,
    title: String,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RecipesHeaderBg)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(1.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(RecipesBrandRed),
                )
                Text(
                    text = "$matchPercent% Match",
                    color = RecipesTextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Text(
            text = title,
            color = RecipesTextGray,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBookmarkToggle,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = "Bookmark",
                tint = RecipesBrandRed,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun RecipeStatsRow(timeMinutes: Int, calories: Int, difficulty: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatItem(icon = Icons.Outlined.AccessTime, text = "$timeMinutes min")
        StatItem(icon = Icons.Filled.Opacity, text = "$calories kcal")
        StatItem(icon = Icons.Filled.BarChart, text = difficulty)
    }
}

@Composable
private fun StatItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RecipesTextGray,
            modifier = Modifier.size(15.dp),
        )
        Text(text = text, color = RecipesTextGray, fontSize = 13.sp)
    }
}

@Composable
private fun RecipeTagsRow(tags: List<RecipeCardTag>) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { tag ->
            RecipeTagChip(tag)
        }
    }
}

@Composable
private fun RecipeTagChip(tag: RecipeCardTag) {
    val bgColor = if (tag.highlighted) Color(0xFFFFF0F0) else Color(0xFFF0F0F0)
    val borderColor = if (tag.highlighted) RecipesBrandRed.copy(alpha = 0.5f) else RecipesTagBorder
    val textColor = if (tag.highlighted) RecipesBrandRed else RecipesTextGray

    Box(
        modifier = Modifier
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        if (tag.highlighted) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(color = borderColor, style = Stroke(width = 1.dp.toPx()))
            }
        }
        Text(
            text = tag.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun AllIngredientsBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA)),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(color = RecipesMissingBorder, style = Stroke(width = 1.dp.toPx()))
        }
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFFFFEEEE), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "!",
                    color = RecipesBrandRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "You have all ingredients",
                color = RecipesTextGray,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun MissingIngredientsBox(ingredients: List<String>) {
    val ingredientText = ingredients.joinToString(", ")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA)),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(color = RecipesMissingBorder, style = Stroke(width = 1.dp.toPx()))
        }
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFFFFEEEE), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "!",
                    color = RecipesBrandRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "Missing: $ingredientText",
                color = RecipesTextGray,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

