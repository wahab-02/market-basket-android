package ai.algo1.marketbasket.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MarketBasketColorScheme = lightColorScheme(
    primary = MarketBasketColors.Primary,
    background = MarketBasketColors.BackgroundWhite,
    surface = MarketBasketColors.BackgroundWhite,
    onPrimary = MarketBasketColors.BackgroundWhite,
    onBackground = MarketBasketColors.TextPrimary,
    onSurface = MarketBasketColors.TextPrimary,
)

/** App theme: Material 3 as substrate, seeded with the brand palette. */
@Composable
fun MarketBasketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MarketBasketColorScheme,
        content = content,
    )
}
