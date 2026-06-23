package ai.algo1.marketbasket.core.domain.connection

import java.net.URLEncoder

object WhatsAppConnect {
    /**
     * Plain connect message for a brand-new user — verbatim from the web WelcomeScreen
     * (src/components/WelcomeScreen.tsx:4, DEFAULT_WHATSAPP_MESSAGE). The webhook mints the
     * publicId itself and returns it via the App-Link `?u=`. We must NOT send "Connect list <id>":
     * the web sends that only for an EXISTING Market Basket imported list (App.tsx:2661-2664,
     * gated on isMarketBasketOnboarding) — for a fresh user it sends this plain message.
     */
    fun connectMessage(): String = "Tap the send button to connect and get started."

    /** wa.me deep link; text encoded encodeURIComponent-style (spaces -> %20). */
    fun connectUri(number: String): String {
        val text = URLEncoder.encode(connectMessage(), "UTF-8").replace("+", "%20")
        return "https://wa.me/$number?text=$text"
    }
}
