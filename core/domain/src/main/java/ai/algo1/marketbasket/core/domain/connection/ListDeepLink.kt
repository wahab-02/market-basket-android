package ai.algo1.marketbasket.core.domain.connection

object ListDeepLink {
    /**
     * QR / App-Link URL that routes into the app: https://<host>/?u=<publicId>.
     * [baseUrl] is the deployed origin (BuildConfig.BACKEND_BASE_URL); a trailing slash is tolerated.
     */
    fun listUrl(baseUrl: String, publicId: String): String =
        baseUrl.trimEnd('/') + "/?u=" + publicId
}
