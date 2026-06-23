package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.identity.Identity
import org.junit.Assert.assertEquals
import org.junit.Test

class IdentityTest {
    @Test fun inboundWins() {
        // The webhook's `?u=` return link (or a shared list link) is authoritative.
        assertEquals(Identity.Resolution.Use("inbound"), Identity.resolve(stored = "stored", inbound = "inbound"))
        assertEquals(Identity.Resolution.Use("inbound"), Identity.resolve(stored = null, inbound = "inbound"))
    }

    @Test fun storedWhenNoInbound() {
        assertEquals(Identity.Resolution.Use("stored"), Identity.resolve(stored = "stored", inbound = null))
        assertEquals(Identity.Resolution.Use("stored"), Identity.resolve(stored = "stored", inbound = "  "))
    }

    @Test fun noneWhenNeither() {
        assertEquals(Identity.Resolution.None, Identity.resolve(stored = null, inbound = null))
        assertEquals(Identity.Resolution.None, Identity.resolve(stored = "", inbound = " "))
    }
}
