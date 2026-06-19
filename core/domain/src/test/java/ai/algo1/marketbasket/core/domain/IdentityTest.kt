package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.identity.Identity
import org.junit.Assert.assertEquals
import org.junit.Test

class IdentityTest {
    @Test fun storedWins() {
        assertEquals(Identity.Resolution.Use("stored"), Identity.resolve(stored = "stored", inbound = "inbound"))
    }

    @Test fun inboundWhenNoStored() {
        assertEquals(Identity.Resolution.Use("inbound"), Identity.resolve(stored = null, inbound = "inbound"))
        assertEquals(Identity.Resolution.Use("inbound"), Identity.resolve(stored = "  ", inbound = "inbound"))
    }

    @Test fun mintWhenNeither() {
        assertEquals(Identity.Resolution.Mint, Identity.resolve(stored = null, inbound = null))
        assertEquals(Identity.Resolution.Mint, Identity.resolve(stored = "", inbound = " "))
    }
}
