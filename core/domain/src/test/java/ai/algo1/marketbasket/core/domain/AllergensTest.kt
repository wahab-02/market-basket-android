package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.catalog.Allergens
import org.junit.Assert.assertEquals
import org.junit.Test

class AllergensTest {
    @Test fun matchesInKeywordListOrder() {
        // ALLERGEN_KEYWORDS order: ... wheat ... milk ... soy ...
        assertEquals(
            listOf("wheat", "milk", "soy"),
            Allergens.parseAllergens("Contains milk, soy and wheat"),
        )
    }

    @Test fun substringOverMatchingIsFaithfulToSource() {
        // "peanuts" also contains the substrings "peanut" and "nuts" (TS uses includes())
        assertEquals(
            listOf("peanuts", "peanut", "nuts"),
            Allergens.parseAllergens("May contain peanuts"),
        )
    }

    @Test fun emptyWhenNoAllergenWords() {
        assertEquals(emptyList<String>(), Allergens.parseAllergens(""))
        assertEquals(emptyList<String>(), Allergens.parseAllergens("Suitable for vegans"))
    }
}
