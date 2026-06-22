package ai.algo1.marketbasket.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.BuildConfig
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.connection.ListDeepLink
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ConnectionUiState(
    val greeting: String = "Hi Shopper!",
    val phoneConnected: Boolean = false,
    val listCode: String? = null,
    val qrUrl: String? = null,
)

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    listRepository: ListRepository,
) : ViewModel() {

    val uiState: StateFlow<ConnectionUiState> =
        combine(listRepository.userProfile, listRepository.publicId) { profile, pid ->
            ConnectionUiState(
                greeting = profile?.displayName?.takeIf { it.isNotBlank() }?.let { "Hi $it" } ?: "Hi Shopper!",
                phoneConnected = profile?.phoneConnected ?: false,
                listCode = profile?.listCode,
                qrUrl = pid?.let { ListDeepLink.listUrl(BuildConfig.BACKEND_BASE_URL, it) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionUiState())
}
