package ai.algo1.marketbasket.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.algo1.marketbasket.feature.onboarding.qr.qrBitmap

@Composable
fun ConnectionRoute(viewModel: ConnectionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ConnectionScreen(state)
}

@Composable
internal fun ConnectionScreen(state: ConnectionUiState) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFFF7F7F7)).verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(state.greeting, color = OnboardingColors.Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())

        Surface(
            color = if (state.phoneConnected) OnboardingColors.WhatsAppGreen else OnboardingColors.ChipBg,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(
                if (state.phoneConnected) "WhatsApp connected" else "Not connected",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = if (state.phoneConnected) Color.White else OnboardingColors.Muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Scan to open this list", color = OnboardingColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                val density = LocalDensity.current
                val px = remember(density) { with(density) { 160.dp.roundToPx() } }
                val qr = remember(state.qrUrl, px) { state.qrUrl?.let { qrBitmap(it, px) } }
                if (qr != null) {
                    Box(Modifier.size(176.dp).background(Color.White).padding(8.dp), contentAlignment = Alignment.Center) {
                        Image(bitmap = qr, contentDescription = "QR code for your list", modifier = Modifier.size(160.dp))
                    }
                }
                state.listCode?.let { code ->
                    Text("List code: $code", color = OnboardingColors.Muted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
