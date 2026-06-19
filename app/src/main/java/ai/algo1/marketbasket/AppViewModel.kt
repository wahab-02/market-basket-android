package ai.algo1.marketbasket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.local.LocalStore
import ai.algo1.marketbasket.core.data.repository.ListRepository
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

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private var started = false

    /** Called once from the Activity with the inbound `?u=` value (if any) from an App Link. */
    fun bootstrap(inbound: String?) {
        if (started) return
        started = true
        viewModelScope.launch {
            val stored = localStore.publicId.first()
            val publicId = when (val r = Identity.resolve(stored, inbound)) {
                is Identity.Resolution.Use -> r.publicId
                Identity.Resolution.Mint -> UUID.randomUUID().toString()
            }
            if (publicId != stored) localStore.setPublicId(publicId)
            listRepository.load(publicId)
            listRepository.observeRealtime(viewModelScope)
            _ready.value = true
        }
    }
}
