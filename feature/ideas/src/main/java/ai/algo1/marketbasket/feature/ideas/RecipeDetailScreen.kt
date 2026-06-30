package ai.algo1.marketbasket.feature.ideas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DetailRed = Color(0xFFB83040)
private val DetailDarkRed = Color(0xFF8B1A2A)
private val DetailTextDark = Color(0xFF111111)
private val DetailTextGray = Color(0xFF888888)
private val DetailBorder = Color(0xFFE0E0E0)

@Composable
internal fun RecipeDetailScreen(
    recipe: RecipeCardItem,
    onBack: () -> Unit,
) {
    var isBookmarked by remember { mutableStateOf(recipe.isBookmarked) }
    val missingCount = recipe.ingredients.count { !it.inList }
    val inListCount = recipe.ingredients.count { it.inList }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Image(
                    painter = painterResource(recipe.imageRes),
                    contentDescription = recipe.fullTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp),
                ) {
                    Text(
                        text = recipe.fullTitle,
                        color = DetailRed,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp,
                    )

                    if (recipe.description.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = recipe.description,
                            color = DetailTextGray,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatColumn(label = "Time", value = "${recipe.timeMinutes} min")
                        Box(Modifier.width(1.dp).height(40.dp).background(DetailBorder))
                        StatColumn(label = "Servings", value = "${recipe.servings}")
                        Box(Modifier.width(1.dp).height(40.dp).background(DetailBorder))
                        StatColumn(label = "Calories", value = "${recipe.calories}")
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(24.dp))

                    if (recipe.ingredients.isNotEmpty()) {
                        Text(
                            text = "Ingredients",
                            color = DetailTextDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(16.dp))

                        IngredientsBadgeRow(
                            icon = { CheckSquareIcon() },
                            label = "Already in List",
                            count = inListCount,
                        )
                        Spacer(Modifier.height(8.dp))
                        IngredientsBadgeRow(
                            icon = { ExclamationIcon() },
                            label = "Missing Items",
                            count = missingCount,
                        )

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        recipe.ingredients.forEachIndexed { index, ingredient ->
                            IngredientRow(ingredient = ingredient, onAdd = {})
                            if (index < recipe.ingredients.lastIndex) {
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                            }
                        }

                        if (missingCount > 0) {
                            Spacer(Modifier.height(16.dp))
                            AddMissingButton(count = missingCount, onClick = {})
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(24.dp))
                    }

                    if (recipe.instructions.isNotEmpty()) {
                        Text(
                            text = "Instructions",
                            color = DetailTextDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(16.dp))

                        recipe.instructions.forEachIndexed { index, step ->
                            InstructionRow(stepNumber = index + 1, text = step)
                            if (index < recipe.instructions.lastIndex) {
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.navigationBarsPadding().height(120.dp))
                }
            }
        }

        // Floating top buttons — drawn on top of the scroll content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DetailIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DetailTextDark,
                    modifier = Modifier.size(20.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailIconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = DetailTextDark,
                        modifier = Modifier.size(20.dp),
                    )
                }
                DetailIconButton(onClick = { isBookmarked = !isBookmarked }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.Bookmark,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) DetailRed else DetailTextDark,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(color = DetailBorder, style = Stroke(width = 1.dp.toPx()))
        }
        content()
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = DetailTextGray, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = value, color = DetailTextDark, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun IngredientsBadgeRow(
    icon: @Composable () -> Unit,
    label: String,
    count: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Text(
            text = label,
            color = DetailTextGray,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Surface(
            color = DetailDarkRed,
            shape = RoundedCornerShape(2.dp),
        ) {
            Text(
                text = "$count item${if (count != 1) "s" else ""}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun CheckSquareIcon() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(DetailRed, RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ExclamationIcon() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(DetailDarkRed, RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "!", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun IngredientRow(ingredient: RecipeIngredient, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalOffer,
                contentDescription = null,
                tint = DetailTextGray,
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = ingredient.name,
            color = DetailTextDark,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = ingredient.quantity,
            color = DetailTextGray,
            fontSize = 14.sp,
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAdd,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(color = Color(0xFFDDDDDD), style = Stroke(width = 1.dp.toPx()))
            }
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add to list",
                tint = DetailRed,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AddMissingButton(count: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(color = DetailRed, style = Stroke(width = 1.5.dp.toPx()))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = null,
                tint = DetailRed,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Add $count missing item${if (count != 1) "s" else ""} to list",
                color = DetailRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun InstructionRow(stepNumber: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(DetailDarkRed),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$stepNumber",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = text,
            color = DetailTextDark,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f),
        )
    }
}
