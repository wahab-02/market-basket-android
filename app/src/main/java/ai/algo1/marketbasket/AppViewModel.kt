package ai.algo1.marketbasket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.local.LocalStore
import ai.algo1.marketbasket.core.data.remote.ApiService
import ai.algo1.marketbasket.core.data.remote.ImportItem
import ai.algo1.marketbasket.core.data.remote.ImportRequest
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.catalog.CategoryResolver
import ai.algo1.marketbasket.core.domain.connection.Connection
import ai.algo1.marketbasket.core.domain.connection.ConnectionState
import ai.algo1.marketbasket.core.domain.identity.Identity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Base64
import javax.inject.Inject

data class InboundLink(
    val publicId: String?,
    val marketBasketImport: String?,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val localStore: LocalStore,
    private val listRepository: ListRepository,
    private val apiService: ApiService,
) : ViewModel() {

    private val _connectionState = MutableStateFlow(ConnectionState.Loading)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /** The resolved anonymous publicId (null until the webhook mints one and returns it via `?u=`). */
    val publicId: StateFlow<String?> = listRepository.publicId

    private var started = false
    private var realtimeStarted = false
    private var evaluating = false
    private var resolvedPublicId: String? = null

    /** Called once from the Activity with the inbound `?u=` value (if any) from an App Link. */
    fun bootstrap(inbound: InboundLink) {
        if (started) return
        started = true
        viewModelScope.launch {
            val stored = localStore.publicId.first()
            when (val r = Identity.resolve(stored, inbound.publicId)) {
                is Identity.Resolution.Use -> adopt(r.publicId, stored, inbound.marketBasketImport)
                Identity.Resolution.None -> {
                    // Brand-new device: no id yet. The webhook mints it on WhatsApp connect and
                    // returns it via the App Link (handled in onInbound). Until then, unconnected.
                    resolvedPublicId = null
                    _connectionState.value = ConnectionState.Unconnected
                }
            }
        }
    }

    /**
     * Called from Activity.onNewIntent when a `?u=` App Link arrives while the app is running —
     * e.g. the WhatsApp connect-return link `…/?u=<publicId>&linked=whatsapp`.
     */
    fun onInbound(inbound: InboundLink) {
        val id = inbound.publicId?.trim()?.ifBlank { null } ?: return
        viewModelScope.launch {
            val stored = localStore.publicId.first()
            if (id != resolvedPublicId) realtimeStarted = false   // switching lists → allow realtime to (re)start
            adopt(id, stored, inbound.marketBasketImport)
        }
    }

    private suspend fun adopt(publicId: String, stored: String?, marketBasketImport: String? = null) {
        if (publicId != stored) localStore.setPublicId(publicId)
        resolvedPublicId = publicId
        if (!marketBasketImport.isNullOrBlank() && importMarketBasket(publicId, marketBasketImport)) {
            localStore.setConnected(true)
            realtimeStarted = false
        }
        evaluate(publicId)
    }

    /** Re-evaluate connection (Activity onResume) — recovers a session whose list hasn't loaded yet. */
    fun recheckConnection() {
        val pid = resolvedPublicId ?: return
        if (realtimeStarted) return
        viewModelScope.launch { evaluate(pid) }
    }

    private suspend fun evaluate(publicId: String) {
        if (evaluating) return
        evaluating = true
        try {
            val cached = localStore.connected.first()
            val rowExists = try {
                listRepository.load(publicId)
            } catch (e: Exception) {
                false   // offline / transient: fall back to the cached flag below
            }
            val connected = cached || rowExists
            if (connected) {
                if (rowExists && !cached) localStore.setConnected(true)
                if (rowExists && !realtimeStarted) {
                    listRepository.observeRealtime(viewModelScope)
                    realtimeStarted = true
                }
            }
            _connectionState.value = if (connected) ConnectionState.Connected else ConnectionState.Unconnected
        } finally {
            evaluating = false
        }
    }

    private suspend fun importMarketBasket(publicId: String, encodedPayload: String): Boolean {
        val items = decodeMarketBasketImport(encodedPayload)
        if (items.isEmpty()) return false

        return try {
            apiService.importMarketBasket(ImportRequest(publicId = publicId, items = items))
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun decodeMarketBasketImport(encodedPayload: String): List<ImportItem> {
        val padded = encodedPayload.padEnd(encodedPayload.length + ((4 - encodedPayload.length % 4) % 4), '=')
        val json = String(Base64.getUrlDecoder().decode(padded), Charsets.UTF_8)
        val payload = JSONObject(json)
        val items = payload.optJSONArray("items") ?: return emptyList()

        return buildList {
            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val name = item.optString("name").trim()
                if (name.isEmpty()) continue

                val quantity = when {
                    item.has("qty") -> item.optInt("qty", 1)
                    else -> item.optInt("quantity", 1)
                }.coerceIn(1, 99)
                val category = CategoryResolver.resolveCategoryFromText(
                    rawCategory = item.optString("category").takeIf { it.isNotBlank() },
                    productName = name,
                )

                add(ImportItem(name = name, quantity = quantity, category = category))
            }
        }
    }
}
