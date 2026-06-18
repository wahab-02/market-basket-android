package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.util.StringSimilarity.similarity
import org.junit.Assert.assertEquals
import org.junit.Test

class StringSimilarityTest {
    private val delta = 1e-9

    @Test fun exactMatchCaseInsensitive() {
        assertEquals(1.0, similarity("milk", "milk"), delta)
        assertEquals(1.0, similarity("milk", "Milk"), delta)
    }

    @Test fun containmentScores085() {
        assertEquals(0.85, similarity("milk", "whole milk"), delta)
        assertEquals(0.85, similarity("whole milk", "milk"), delta)
    }

    @Test fun wordOverlapJaccard() {
        // {red,wine} vs {white,wine}: 1 shared / 3 union
        assertEquals(1.0 / 3.0, similarity("red wine", "white wine"), delta)
    }

    @Test fun noOverlapIsZero() {
        assertEquals(0.0, similarity("apple", "banana"), delta)
    }
}
