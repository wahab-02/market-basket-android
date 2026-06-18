package ai.algo1.marketbasket.core.domain.theme

/**
 * Raw ARGB color values copied 1:1 from src/lib/colors.ts.
 * Kept as platform-agnostic Longs here (unit-testable); :core:designsystem wraps them in Compose Color.
 * rgba alphas: 0.6 -> 0x99, 0.4 -> 0x66, 0.1 -> 0x1A.
 */
object BrandPalette {
    const val PRIMARY = 0xFF1E3932L
    const val BACKGROUND_WHITE = 0xFFFFFFFFL
    const val BACKGROUND_ALT = 0xFFF1F1F1L
    const val DEALS_BANNER = 0xFFC596FFL
    const val CHECKBOX_CHECKED = 0xFF009476L
    const val CHECKBOX_UNCHECKED = 0xFFBBBBBBL
    const val TEXT_PRIMARY = 0xFF080816L
    const val TEXT_SECONDARY = 0x99080816L
    const val TEXT_MUTED = 0x66080816L
    const val IMAGE_PLACEHOLDER = 0xFFF1F1F1L
    const val DIVIDER = 0x1A080816L
}
