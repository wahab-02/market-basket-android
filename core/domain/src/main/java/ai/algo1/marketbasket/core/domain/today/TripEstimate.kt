package ai.algo1.marketbasket.core.domain.today

import kotlin.math.ceil
import kotlin.math.max

/** Pure port of estimateTripMinutes (TodayPage.tsx). */
object TripEstimate {
    fun estimateTripMinutes(itemCount: Int, categoryCount: Int): Int {
        if (itemCount <= 0) return 1
        val pickingMinutes = itemCount * 0.75
        val aisleSwitchMinutes = max(categoryCount - 1, 0) * 2
        return max(1, ceil(pickingMinutes + aisleSwitchMinutes).toInt())
    }
}
