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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val localStore: LocalStore,
    private val listRepository: ListRepository,
) : ViewModel() {

    private val _connectionState = MutableStateFlow(ConnectionState.Loading)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /** The resolved anonymous publicId (needed to build the WhatsApp connect link). */
    val publicId: StateFlow<String?> = listRepository.publicId

    private var started = false
    private var realtimeStarted = false
    private var resolvedPublicId: String? = null

    /** Called once from the Activity with the inbound `?u=` value (if any) from an App Link. */
    fun bootstrap(inbound: String?) {
        if (started) return
        started = true
        viewModelScope.launch {
            val stored = localStore.publicId.first()
            val resolved = when (val r = Identity.resolve(stored, inbound)) {
                is Identity.Resolution.Use -> r.publicId
                Identity.Resolution.Mint -> UUID.randomUUID().toString()
            }
            if (resolved != stored) localStore.setPublicId(resolved)
            resolvedPublicId = resolved
            evaluate(resolved)
        }
    }

    /** Re-evaluate connection (called from the Activity onResume), e.g. after returning from WhatsApp. */
    fun recheckConnection() {
        val pid = resolvedPublicId ?: return
        if (_connectionState.value == ConnectionState.Connected) return
        viewModelScope.launch { evaluate(pid) }
    }

    private suspend fun evaluate(publicId: String) {
        val cached = localStore.connected.first()
        val rowExists = try {
            listRepository.load(publicId)
        } catch (e: Exception) {
            false   // offline / transient: fall back to the cached flag below
        }
        val state = Connection.resolve(cachedConnected = cached, rowExists = rowExists)
        if (state == ConnectionState.Connected) {
            if (rowExists && !cached) localStore.setConnected(true)
            if (!realtimeStarted) {
                listRepository.observeRealtime(viewModelScope)
                realtimeStarted = true
            }
        }
        _connectionState.value = state
    }
}
