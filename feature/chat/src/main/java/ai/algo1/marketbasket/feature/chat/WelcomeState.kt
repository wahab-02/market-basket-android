package ai.algo1.marketbasket.feature.chat

import ai.algo1.marketbasket.core.domain.util.Greetings
import java.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SuggestionBorder = Color(0xFFE5E7EB)
private val SuggestionText   = Color(0xFF525252)

@Composable
fun WelcomeState(
    suggestions: List<String>,
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))

        Image(
            painter = painterResource(R.drawable.market_basket),
            contentDescription = "Market Basket",
            contentScale = ContentScale.Fit,
            modifier = Modifier.width(192.dp),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "${Greetings.greetingFor(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))}, Market Basket Shopper!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )

        Spacer(Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            suggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onSuggestion(suggestion) },
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SuggestionBorder),
                    shadowElevation = 1.dp,
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 16.sp,
                        color = SuggestionText,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }
            }
        }
    }
}
