package ai.algo1.marketbasket.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** First-run overlay shown over the Today tab until dismissed. No-op once seen. */
@Composable
fun HomeIntroOverlay(viewModel: HomeIntroViewModel = hiltViewModel()) {
    val show by viewModel.showIntro.collectAsStateWithLifecycle()
    if (!show) return
    Box(
        Modifier.fillMaxSize()
            .background(OnboardingColors.Scrim)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { viewModel.dismiss() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.home_popup),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
                Text("One list.", color = OnboardingColors.Ink, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 16.dp))
                Text("Everything", color = OnboardingColors.Ink, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("connected.", color = OnboardingColors.WhatsAppGreen, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Market Basket brings your list, deals, recipes, and ideas together so you can save money, time and eat better.",
                    color = OnboardingColors.Muted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Surface(
                    color = OnboardingColors.Ink,
                    shape = RoundedCornerShape(999.dp),
                    onClick = { viewModel.dismiss() },
                    modifier = Modifier.padding(top = 20.dp).fillMaxWidth().height(52.dp),
                ) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Got it", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
