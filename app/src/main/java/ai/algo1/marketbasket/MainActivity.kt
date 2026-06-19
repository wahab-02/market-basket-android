package ai.algo1.marketbasket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ai.algo1.marketbasket.core.designsystem.MarketBasketTheme
import ai.algo1.marketbasket.nav.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inbound App Link: https://<host>/?u=<publicId>
        val inbound = intent?.data?.getQueryParameter("u")
        appViewModel.bootstrap(inbound)
        setContent {
            MarketBasketTheme {
                AppNavHost()
            }
        }
    }
}
