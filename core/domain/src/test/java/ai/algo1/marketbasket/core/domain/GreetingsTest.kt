package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.util.Greetings
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingsTest {
    // greetings.ts: h<12 -> Good Morning; h<17 -> Good Afternoon; else Good Evening
    @Test fun morning_below12() {
        assertEquals("Good Morning", Greetings.greetingFor(0))
        assertEquals("Good Morning", Greetings.greetingFor(11))
    }

    @Test fun afternoon_12to16() {
        assertEquals("Good Afternoon", Greetings.greetingFor(12))
        assertEquals("Good Afternoon", Greetings.greetingFor(16))
    }

    @Test fun evening_17andLater() {
        assertEquals("Good Evening", Greetings.greetingFor(17))
        assertEquals("Good Evening", Greetings.greetingFor(23))
    }
}
