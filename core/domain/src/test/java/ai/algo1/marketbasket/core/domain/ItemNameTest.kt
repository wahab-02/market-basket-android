package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.util.ItemName
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemNameTest {
    // normalizeItemName: trim -> collapse internal whitespace -> lowercase
    @Test fun trimsCollapsesAndLowercases() {
        assertEquals("whole milk", ItemName.normalize("  Whole   Milk  "))
        assertEquals("eggs", ItemName.normalize("EGGS"))
        assertEquals("a b c", ItemName.normalize("a\t b\n  c"))
    }
}
