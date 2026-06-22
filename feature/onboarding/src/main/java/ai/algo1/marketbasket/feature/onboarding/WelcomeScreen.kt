package ai.algo1.marketbasket.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val previewChips = listOf("All", "Produce", "Bakery", "Dairy")
private val previewItems = listOf("Milk", "Bread", "Bananas", "Eggs", "Chicken breast", "Tomatoes")

@Composable
fun WelcomeScreen(onConnect: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
        ListBackground(Modifier.fillMaxSize().blur(8.dp))
        Box(Modifier.fillMaxSize().background(OnboardingColors.Scrim))
        WhatsAppConnectDrawer(onConnect = onConnect, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ListBackground(modifier: Modifier = Modifier) {
    Column(modifier.padding(20.dp)) {
        Text("Hi!", color = OnboardingColors.Muted, fontSize = 16.sp)
        Text("Your shopping list", color = OnboardingColors.Ink, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            previewChips.forEach { chip ->
                Surface(color = OnboardingColors.ChipBg, shape = RoundedCornerShape(999.dp)) {
                    Text(chip, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = OnboardingColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Column(Modifier.padding(top = 20.dp)) {
            previewItems.forEach { item ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                ) {
                    Text(item, modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp), color = OnboardingColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
