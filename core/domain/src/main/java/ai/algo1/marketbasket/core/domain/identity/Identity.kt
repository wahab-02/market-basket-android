package ai.algo1.marketbasket.core.domain.identity

/** Pure anonymous-identity resolution: stored publicId wins, then an inbound deep-link id, else mint. */
object Identity {
    sealed interface Resolution {
        data class Use(val publicId: String) : Resolution
        data object Mint : Resolution
    }

    fun resolve(stored: String?, inbound: String?): Resolution = when {
        !stored.isNullOrBlank() -> Resolution.Use(stored.trim())
        !inbound.isNullOrBlank() -> Resolution.Use(inbound.trim())
        else -> Resolution.Mint
    }
}
