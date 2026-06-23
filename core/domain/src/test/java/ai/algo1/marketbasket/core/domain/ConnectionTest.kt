package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.connection.Connection
import ai.algo1.marketbasket.core.domain.connection.ConnectionState
import ai.algo1.marketbasket.core.domain.connection.ListDeepLink
import ai.algo1.marketbasket.core.domain.connection.UserProfile
import ai.algo1.marketbasket.core.domain.connection.WhatsAppConnect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionTest {
    @Test fun resolve_unconnected_whenNoRowAndNotCached() {
        assertEquals(ConnectionState.Unconnected, Connection.resolve(cachedConnected = false, rowExists = false))
    }

    @Test fun resolve_connected_whenRowExists() {
        assertEquals(ConnectionState.Connected, Connection.resolve(cachedConnected = false, rowExists = true))
    }

    @Test fun resolve_connected_whenCachedEvenIfNoRow() {
        assertEquals(ConnectionState.Connected, Connection.resolve(cachedConnected = true, rowExists = false))
    }

    @Test fun userProfile_phoneConnected_reflectsPhonePresence() {
        assertTrue(UserProfile("Sam", "1555", "ABC123").phoneConnected)
        assertFalse(UserProfile("Sam", null, null).phoneConnected)
        assertFalse(UserProfile("Sam", "   ", null).phoneConnected)
    }

    @Test fun connectMessage_isPlainWebDefault() {
        assertEquals(
            "Tap the send button to connect and get started.",
            WhatsAppConnect.connectMessage(),
        )
    }

    @Test fun connectUri_encodesPlainMessage() {
        assertEquals(
            "https://wa.me/17177449812?text=Tap%20the%20send%20button%20to%20connect%20and%20get%20started.",
            WhatsAppConnect.connectUri("17177449812"),
        )
    }

    @Test fun listUrl_buildsDeepLink() {
        assertEquals(
            "https://algo1-webhook.vercel.app/?u=123",
            ListDeepLink.listUrl("https://algo1-webhook.vercel.app", "123"),
        )
    }

    @Test fun listUrl_toleratesTrailingSlash() {
        assertEquals(
            "https://algo1-webhook.vercel.app/?u=123",
            ListDeepLink.listUrl("https://algo1-webhook.vercel.app/", "123"),
        )
    }
}
