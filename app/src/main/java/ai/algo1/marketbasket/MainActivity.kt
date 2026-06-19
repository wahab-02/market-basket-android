package ai.algo1.marketbasket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.algo1.marketbasket.core.designsystem.MarketBasketTheme
import ai.algo1.marketbasket.nav.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inbound App Link: https://<host>/?u=<publicId>
        val inbound = intent.data?.getQueryParameter("u")
        appViewModel.bootstrap(inbound)
        setContent {
            MarketBasketTheme {
                // Gate initial render on identity bootstrap + first list load (mirrors the web app's loading flag).
                val ready by appViewModel.ready.collectAsStateWithLifecycle()
                if (ready) AppNavHost() else LoadingScreen()
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
