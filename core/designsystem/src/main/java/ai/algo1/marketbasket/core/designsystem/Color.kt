package ai.algo1.marketbasket.core.designsystem

import ai.algo1.marketbasket.core.domain.theme.BrandPalette
import androidx.compose.ui.graphics.Color

/** Compose colors built from the unit-tested BrandPalette values (single source of truth). */
object MarketBasketColors {
    val Primary = Color(BrandPalette.PRIMARY)
    val BackgroundWhite = Color(BrandPalette.BACKGROUND_WHITE)
    val BackgroundAlt = Color(BrandPalette.BACKGROUND_ALT)
    val DealsBanner = Color(BrandPalette.DEALS_BANNER)
    val CheckboxChecked = Color(BrandPalette.CHECKBOX_CHECKED)
    val CheckboxUnchecked = Color(BrandPalette.CHECKBOX_UNCHECKED)
    val TextPrimary = Color(BrandPalette.TEXT_PRIMARY)
    val TextSecondary = Color(BrandPalette.TEXT_SECONDARY)
    val TextMuted = Color(BrandPalette.TEXT_MUTED)
    val ImagePlaceholder = Color(BrandPalette.IMAGE_PLACEHOLDER)
    val Divider = Color(BrandPalette.DIVIDER)
}
