package ai.algo1.marketbasket.core.data.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Minimal interface exposing only [publicId] — used so [ChatViewModel] can be tested
 * without depending on the concrete (final) [ListRepository] class.
 */
interface PublicIdProvider {
    val publicId: StateFlow<String?>
}
