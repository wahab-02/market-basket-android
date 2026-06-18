package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.theme.BrandPalette
import org.junit.Assert.assertEquals
import org.junit.Test

class BrandPaletteTest {
    @Test
    fun opaqueBrandColors_matchColorsTs() {
        assertEquals(0xFF1E3932L, BrandPalette.PRIMARY)
        assertEquals(0xFFF1F1F1L, BrandPalette.BACKGROUND_ALT)
        assertEquals(0xFFC596FFL, BrandPalette.DEALS_BANNER)
        assertEquals(0xFF009476L, BrandPalette.CHECKBOX_CHECKED)
        assertEquals(0xFF080816L, BrandPalette.TEXT_PRIMARY)
    }

    @Test
    fun translucentTextColors_useCorrectArgbAlpha() {
        // colors.ts: textSecondary rgba(8,8,22,0.6) -> 0x99; textMuted 0.4 -> 0x66; divider 0.1 -> 0x1A
        assertEquals(0x99080816L, BrandPalette.TEXT_SECONDARY)
        assertEquals(0x66080816L, BrandPalette.TEXT_MUTED)
        assertEquals(0x1A080816L, BrandPalette.DIVIDER)
    }
}
