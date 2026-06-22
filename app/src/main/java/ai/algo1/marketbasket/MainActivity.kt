package ai.algo1.marketbasket

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import ai.algo1.marketbasket.core.data.BuildConfig
import ai.algo1.marketbasket.core.designsystem.MarketBasketTheme
import ai.algo1.marketbasket.core.domain.connection.ConnectionState
import ai.algo1.marketbasket.core.domain.connection.WhatsAppConnect
import ai.algo1.marketbasket.feature.onboarding.WelcomeScreen
import ai.algo1.marketbasket.nav.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inbound App Link: https://algo1-webhook.vercel.app/?u=<publicId>
        val inbound = intent.data?.getQueryParameter("u")
        appViewModel.bootstrap(inbound)
        setContent {
            MarketBasketTheme {
                val state by appViewModel.connectionState.collectAsStateWithLifecycle()
                val publicId by appViewModel.publicId.collectAsStateWithLifecycle()
                when (state) {
                    ConnectionState.Loading -> LoadingScreen()
                    ConnectionState.Unconnected ->
                        WelcomeScreen(onConnect = { publicId?.let { openWhatsApp(it) } })
                    ConnectionState.Connected -> AppNavHost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appViewModel.recheckConnection()
    }

    private fun openWhatsApp(publicId: String) {
        val uri = WhatsAppConnect.connectUri(BuildConfig.WHATSAPP_NUMBER, publicId)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (e: ActivityNotFoundException) {
            // No browser/WhatsApp handler; nothing to do — the user can retry.
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
