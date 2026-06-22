package ai.algo1.marketbasket.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private data class OnboardingSlide(val title: String, val desc: String, @DrawableRes val image: Int)

private val onboardingSlides = listOf(
    OnboardingSlide("Text via WhatsApp", "Message an item and it appears here instantly, in real time", R.drawable.whatsapp),
    OnboardingSlide("Share with your household", "Everyone adds to one list no duplicates, no \"I thought you got that\"", R.drawable.family),
    OnboardingSlide("Add with Alexa", "\"Alexa, add milk\" it's on your list instantly, no typing", R.drawable.alexa),
)

@Composable
internal fun WhatsAppConnectDrawer(onConnect: () -> Unit, modifier: Modifier = Modifier) {
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            index = (index + 1) % onboardingSlides.size
        }
    }
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.market_basket_logo),
                contentDescription = "Market Basket",
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(28.dp),
            )
            Text(
                "Your household shopping list",
                modifier = Modifier.padding(top = 12.dp),
                color = OnboardingColors.Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            val slide = onboardingSlides[index]
            Image(
                painter = painterResource(slide.image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(top = 20.dp).size(140.dp),
            )
            Text(
                slide.title,
                modifier = Modifier.padding(top = 16.dp),
                color = OnboardingColors.Ink,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                slide.desc,
                modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp),
                color = OnboardingColors.Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                onboardingSlides.indices.forEach { i ->
                    Box(
                        Modifier.size(8.dp).background(
                            if (i == index) OnboardingColors.WhatsAppGreen else OnboardingColors.DotInactive,
                            CircleShape,
                        )
                    )
                }
            }

            Surface(
                color = OnboardingColors.WhatsAppGreen,
                shape = RoundedCornerShape(999.dp),
                onClick = onConnect,
                modifier = Modifier.padding(top = 22.dp).fillMaxWidth().height(54.dp),
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Connect with WhatsApp", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Takes 10 seconds · No app download needed",
                color = OnboardingColors.Muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
