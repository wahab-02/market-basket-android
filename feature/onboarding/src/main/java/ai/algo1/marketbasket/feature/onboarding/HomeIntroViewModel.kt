package ai.algo1.marketbasket.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.local.LocalStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeIntroViewModel @Inject constructor(
    private val localStore: LocalStore,
) : ViewModel() {

    val showIntro: StateFlow<Boolean> =
        localStore.homeIntroSeen.map { !it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun dismiss() {
        viewModelScope.launch { localStore.setHomeIntroSeen() }
    }
}
