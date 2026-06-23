package ai.algo1.marketbasket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.local.LocalStore
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.connection.Connection
import ai.algo1.marketbasket.core.domain.connection.ConnectionState
import ai.algo1.marketbasket.core.domain.identity.Identity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val localStore: LocalStore,
    private val listRepository: ListRepository,
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
    fun bootstrap(inbound: String?) {
        if (started) return
        started = true
        viewModelScope.launch {
            val stored = localStore.publicId.first()
            when (val r = Identity.resolve(stored, inbound)) {
                is Identity.Resolution.Use -> adopt(r.publicId, stored)
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
    fun onInbound(inbound: String?) {
        val id = inbound?.trim()?.ifBlank { null } ?: return
        viewModelScope.launch {
            val stored = localStore.publicId.first()
            if (id != resolvedPublicId) realtimeStarted = false   // switching lists → allow realtime to (re)start
            adopt(id, stored)
        }
    }

    private suspend fun adopt(publicId: String, stored: String?) {
        if (publicId != stored) localStore.setPublicId(publicId)
        resolvedPublicId = publicId
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
}
