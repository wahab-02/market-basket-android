package ai.algo1.marketbasket.feature.chat.artifacts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.feature.chat.Artifact
import ai.algo1.marketbasket.feature.chat.RecipeIngredient

private val GreenGradient = Brush.linearGradient(listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7)))
private val CardBorder = Color(0xFFE8E9ED)
private val TagBg = Color(0xB3FFFFFF)
private val TagText = Color(0xFF15803D)
private val MetaText = Color(0xFF374151)
private val IngText = Color(0xFF374151)
private val IngStrikeText = Color(0xFF9CA3AF)
private val CheckActive = Color(0xFF16A34A)
private val CheckBorder = Color(0xFFD1D5DB)
private val StepsBlue = Color(0xFF2563EB)
private val BrandInk = Color(0xFF080816)
private val MintGreen = Color(0xFF34D399)
private val AddedBg = Color(0xFFDCFCE7)
private val AddedText = Color(0xFF15803D)

@Composable
fun RecipeCard(
    artifact: Artifact.Recipe,
    addedToList: Boolean,
    onAddIngredients: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = artifact.data
    val totalTime = data.prepTimeMinutes + data.cookTimeMinutes
    var checkedIngredients by remember { mutableStateOf(emptySet<Int>()) }
    var stepsOpen by remember { mutableStateOf(false) }
    var cookingOpen by remember { mutableStateOf(false) }

    CookingModal(title = data.title, steps = data.steps, open = cookingOpen, onClose = { cookingOpen = false })

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 2.dp,
    ) {
        Column {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenGradient)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    data.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(TagBg, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(tag.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TagText)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(data.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = BrandInk)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetaChip(Icons.Filled.AccessTime, "$totalTime min")
                    MetaChip(Icons.Filled.Group, "${data.servings} servings")
                    MetaChip(null, data.cuisine)
                }
            }

            // Ingredients
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "Ingredients".uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp,
                )
                Spacer(Modifier.height(8.dp))
                data.ingredients.forEachIndexed { i, ing ->
                    IngredientRow(
                        ingredient = ing,
                        checked = checkedIngredients.contains(i),
                        onToggle = {
                            checkedIngredients = if (checkedIngredients.contains(i))
                                checkedIngredients - i else checkedIngredients + i
                        },
                    )
                    if (i < data.ingredients.lastIndex) Spacer(Modifier.height(6.dp))
                }
            }

            // Steps toggle
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "${if (stepsOpen) "Hide" else "Show"} steps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StepsBlue,
                    modifier = Modifier
                        .clickable { stepsOpen = !stepsOpen }
                        .padding(vertical = 4.dp),
                )
            }
            AnimatedVisibility(visible = stepsOpen) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    data.steps.forEachIndexed { i, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("${i + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            }
                            Text(step, fontSize = 14.sp, color = MetaText, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Start Cooking
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MintGreen)
                        .clickable { cookingOpen = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Start Cooking", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandInk)
                }
                // Add to list / Added
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (addedToList) AddedBg else BrandInk)
                        .clickable(enabled = !addedToList) {
                            val names = data.ingredients.map { "${it.quantity} ${it.name}".trim() }
                            onAddIngredients(names)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (addedToList) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = AddedText,
                            )
                        }
                        Text(
                            if (addedToList) "Added" else "Add to list",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (addedToList) AddedText else Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: RecipeIngredient, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(2.dp, if (checked) CheckActive else CheckBorder, RoundedCornerShape(4.dp))
                .background(if (checked) CheckActive else Color.Transparent, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.White,
                )
            }
        }
        Text(
            "${ingredient.quantity} ${ingredient.name}",
            fontSize = 14.sp,
            color = if (checked) IngStrikeText else IngText,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetaChip(icon: ImageVector?, label: String) {
    Row(
        modifier = Modifier
            .background(Color(0x99FFFFFF), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MetaText)
        }
        Text(label, fontSize = 12.sp, color = MetaText)
    }
}
