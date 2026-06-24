package ai.algo1.marketbasket.feature.chat

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Locale

private val InputBg    = Color(0xFFF3F4F6)
private val BrandInk   = Color(0xFF080816)
private val PlaceColor = Color(0xFF9CA3AF)
private val MicInk     = Color(0xFF6B7280)
private val MicActive  = Color(0xFFEF4444)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatInput(
    onSend: (String) -> Unit,
    onAbort: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var isVoiceBusy by remember { mutableStateOf(false) }
    var pendingVoiceText by remember { mutableStateOf<String?>(null) }
    var userEditedAfterVoice by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun sendCurrentText() {
        val trimmed = text.trim()
        if (trimmed.isBlank() || isLoading) return
        text = ""
        pendingVoiceText = null
        keyboardController?.hide()
        onSend(trimmed)
    }

    LaunchedEffect(pendingVoiceText, isLoading) {
        val transcript = pendingVoiceText ?: return@LaunchedEffect
        delay(600)
        if (!isLoading && !userEditedAfterVoice && text == transcript) {
            text = ""
            pendingVoiceText = null
            onSend(transcript)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        isVoiceBusy = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                .orEmpty()
            val transcript = matches.firstOrNull().orEmpty()
            if (transcript.isNotBlank()) {
                userEditedAfterVoice = false
                text = transcript
                pendingVoiceText = transcript
            }
        }
    }

    fun startVoiceInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Voice input is not available on this device.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask about recipes or your list")
        }

        try {
            isVoiceBusy = true
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            isVoiceBusy = false
            Toast.makeText(context, "Voice input is not available on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startVoiceInput()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 10.dp)
            .background(InputBg, RoundedCornerShape(24.dp))
            .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 36.dp, max = 120.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = text,
                onValueChange = {
                    userEditedAfterVoice = true
                    pendingVoiceText = null
                    text = it
                },
                textStyle = TextStyle(color = BrandInk, fontSize = 15.sp),
                cursorBrush = SolidColor(BrandInk),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isVoiceBusy,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendCurrentText() }),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                if (isVoiceBusy) "Listening..." else "Ask about recipes, your list...",
                                color = PlaceColor,
                                fontSize = 15.sp,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }

        val canSend = text.isNotBlank() && !isLoading
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isVoiceBusy) MicActive else InputBg)
                .alpha(if (isLoading) 0.4f else 1f)
                .clickable(enabled = !isLoading && !isVoiceBusy) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startVoiceInput()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            MicIcon(active = isVoiceBusy)
        }

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandInk)
                .alpha(if (isLoading || canSend) 1f else 0.3f)
                .clickable(enabled = isLoading || canSend) {
                    if (isLoading) {
                        onAbort()
                    } else {
                        sendCurrentText()
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                Box(Modifier.size(12.dp).background(Color.White, RoundedCornerShape(2.dp)))
            } else if (canSend) {
                SendIcon()
            } else {
                SendIcon()
            }
        }
    }
}

@Composable
private fun SendIcon() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val sx = size.width / 24f
        val sy = size.height / 24f
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2.5f * sx,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
        )
        val plane = Path().apply {
            moveTo(22f * sx, 2f * sy)
            lineTo(15f * sx, 22f * sy)
            lineTo(11f * sx, 13f * sy)
            lineTo(2f * sx, 9f * sy)
            close()
        }
        drawPath(plane, color = Color.White, style = stroke)
        drawLine(
            color = Color.White,
            start = androidx.compose.ui.geometry.Offset(22f * sx, 2f * sy),
            end = androidx.compose.ui.geometry.Offset(11f * sx, 13f * sy),
            strokeWidth = 2.5f * sx,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}

@Composable
private fun MicIcon(active: Boolean) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val color = if (active) Color.White else MicInk
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2.2f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
        )
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.38f, size.height * 0.08f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.24f, size.height * 0.52f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.12f),
            style = stroke,
        )
        drawArc(
            color = color,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.38f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.42f),
            style = stroke,
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.8f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.94f),
            strokeWidth = 2.2f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.34f, size.height * 0.94f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.94f),
            strokeWidth = 2.2f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}
