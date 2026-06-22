package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.today.TripEstimate
import org.junit.Assert.assertEquals
import org.junit.Test

class TripEstimateTest {
    @Test fun zeroItems_isOne() = assertEquals(1, TripEstimate.estimateTripMinutes(0, 0))
    @Test fun negativeItems_isOne() = assertEquals(1, TripEstimate.estimateTripMinutes(-5, 3))
    // 3 items: picking 2.25 + aisle (2-1)*2 = 2 -> ceil(4.25) = 5
    @Test fun threeItemsTwoCats_isFive() = assertEquals(5, TripEstimate.estimateTripMinutes(3, 2))
    // 1 item, 1 cat: picking 0.75 + aisle 0 -> ceil(0.75) = 1
    @Test fun oneItem_isOne() = assertEquals(1, TripEstimate.estimateTripMinutes(1, 1))
    // 10 items, 5 cats: 7.5 + (4*2)=8 -> ceil(15.5)=16
    @Test fun tenItemsFiveCats_isSixteen() = assertEquals(16, TripEstimate.estimateTripMinutes(10, 5))
}
