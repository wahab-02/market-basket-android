package ai.algo1.marketbasket.core.domain.util

import java.net.URI
import java.net.URLDecoder
import java.time.Instant

/**
 * Pure port of isSasExpired() in src/hooks/useImageEnrichment.ts.
 * `now` is injected (the TS read `new Date()`), making expiry deterministic in tests.
 * Returns false on a missing `se` param or any parse failure (matches the TS try/catch + no-`se` path).
 */
object SasToken {
    fun isSasExpired(url: String, now: Instant): Boolean {
        return try {
            val se = queryParam(url, "se") ?: return false
            Instant.parse(se) < now
        } catch (e: Exception) {
            false
        }
    }

    private fun queryParam(url: String, key: String): String? {
        val query = URI(url).rawQuery ?: return null
        // NOTE: use the String-charset overload. URLDecoder.decode(String, Charset) is Android
        // API 33+, but this module ships in the app at minSdk 26; "UTF-8" is always supported.
        for (pair in query.split("&")) {
            if (pair.isEmpty()) continue
            val idx = pair.indexOf("=")
            val rawKey = if (idx >= 0) pair.substring(0, idx) else pair
            if (URLDecoder.decode(rawKey, "UTF-8") == key) {
                val rawVal = if (idx >= 0) pair.substring(idx + 1) else ""
                return URLDecoder.decode(rawVal, "UTF-8")
            }
        }
        return null
    }
}
