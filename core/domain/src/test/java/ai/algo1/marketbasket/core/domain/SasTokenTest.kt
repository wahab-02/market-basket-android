package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.util.SasToken
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SasTokenTest {
    private val now = Instant.parse("2025-06-19T00:00:00Z")

    @Test fun expiredWhenSeBeforeNow() {
        val url = "https://acct.blob.core.windows.net/c/blob.jpg?se=2020-01-01T00%3A00%3A00Z&sig=abc"
        assertTrue(SasToken.isSasExpired(url, now))
    }

    @Test fun notExpiredWhenSeAfterNow() {
        val url = "https://acct.blob.core.windows.net/c/blob.jpg?se=2030-01-01T00%3A00%3A00Z&sig=abc"
        assertFalse(SasToken.isSasExpired(url, now))
    }

    @Test fun notExpiredWhenNoSeParam() {
        assertFalse(SasToken.isSasExpired("https://acct.blob.core.windows.net/c/blob.jpg?sig=abc", now))
    }

    @Test fun falseWhenSeUnparseable() {
        assertFalse(SasToken.isSasExpired("https://x/y?se=not-a-date", now))
    }

    @Test fun falseWhenUrlMalformed() {
        assertFalse(SasToken.isSasExpired("::: not a url :::", now))
    }
}
