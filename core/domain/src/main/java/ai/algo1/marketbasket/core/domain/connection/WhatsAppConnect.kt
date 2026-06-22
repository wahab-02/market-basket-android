package ai.algo1.marketbasket.core.domain.connection

import java.net.URLEncoder

object WhatsAppConnect {
    /** Verbatim onboarding connect message (web src/App.tsx:2662-2664), including the blank line. */
    fun connectMessage(publicId: String): String =
        "Tap the send button to connect and get started.\n\nConnect list $publicId"

    /**
     * wa.me deep link. The text is encoded like the web's encodeURIComponent
     * (spaces -> %20, newlines -> %0A): URLEncoder yields '+' for spaces, which we rewrite to %20.
     */
    fun connectUri(number: String, publicId: String): String {
        val text = URLEncoder.encode(connectMessage(publicId), "UTF-8").replace("+", "%20")
        return "https://wa.me/$number?text=$text"
    }
}
