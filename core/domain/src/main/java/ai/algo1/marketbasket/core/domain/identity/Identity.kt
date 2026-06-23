package ai.algo1.marketbasket.core.domain.identity

/**
 * Pure anonymous-identity resolution. An inbound deep-link id — the webhook's WhatsApp-connect
 * return link or a shared `?u=` list link — is authoritative and wins; otherwise the stored id;
 * otherwise None. A brand-new device has NO id until the webhook mints one on connect and returns
 * it via the `?u=` App Link (the web app likewise never mints for a fresh user — the id arrives
 * via the `?u=` redirect, per src/App.tsx and WelcomeScreen).
 */
object Identity {
    sealed interface Resolution {
        data class Use(val publicId: String) : Resolution
        data object None : Resolution
    }

    fun resolve(stored: String?, inbound: String?): Resolution = when {
        !inbound.isNullOrBlank() -> Resolution.Use(inbound.trim())
        !stored.isNullOrBlank() -> Resolution.Use(stored.trim())
        else -> Resolution.None
    }
}
