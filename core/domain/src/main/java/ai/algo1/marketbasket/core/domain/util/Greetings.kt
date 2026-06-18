package ai.algo1.marketbasket.core.domain.util

enum class DayPart { MORNING, AFTERNOON, EVENING }

/** Pure port of src/lib/greetings.ts with the hour injected (was `new Date().getHours()`). */
object Greetings {
    fun dayPartFor(hour: Int): DayPart = when {
        hour < 12 -> DayPart.MORNING
        hour < 17 -> DayPart.AFTERNOON
        else -> DayPart.EVENING
    }

    fun greetingFor(hour: Int): String = when (dayPartFor(hour)) {
        DayPart.MORNING -> "Good Morning"
        DayPart.AFTERNOON -> "Good Afternoon"
        DayPart.EVENING -> "Good Evening"
    }
}
