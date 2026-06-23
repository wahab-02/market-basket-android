package ai.algo1.marketbasket

import org.junit.Assert.assertEquals
import org.junit.Test

class InboundLinkTest {
    @Test
    fun resolvePublicId_usesCurrentIdForImportLinkWithoutInboundPublicId() {
        val inbound = InboundLink(publicId = null, marketBasketImport = "encoded")

        assertEquals("current", inbound.resolvePublicId(stored = "stored", current = "current"))
    }

    @Test
    fun resolvePublicId_usesStoredIdForImportLinkWithoutInboundPublicId() {
        val inbound = InboundLink(publicId = null, marketBasketImport = "encoded")

        assertEquals("stored", inbound.resolvePublicId(stored = "stored", current = null))
    }

    @Test
    fun resolvePublicId_prefersInboundPublicId() {
        val inbound = InboundLink(publicId = " inbound ", marketBasketImport = "encoded")

        assertEquals("inbound", inbound.resolvePublicId(stored = "stored", current = "current"))
    }

    @Test
    fun resolvePublicId_returnsNullWhenNoImportAndNoInboundPublicId() {
        val inbound = InboundLink(publicId = null, marketBasketImport = null)

        assertEquals(null, inbound.resolvePublicId(stored = "stored", current = "current"))
    }
}
