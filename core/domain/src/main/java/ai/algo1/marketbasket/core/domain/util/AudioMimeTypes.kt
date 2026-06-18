package ai.algo1.marketbasket.core.domain.util

/** Pure port of mimeTypeToExtension() in src/services/transcription/whisperProvider.ts. */
object AudioMimeTypes {
    fun mimeTypeToExtension(mimeType: String): String {
        val base = mimeType.split(";")[0].trim()
        return when (base) {
            "audio/webm" -> "webm"
            "audio/ogg" -> "ogg"
            "audio/mp4" -> "mp4"
            "audio/wav", "audio/wave" -> "wav"
            "audio/mpeg" -> "mp3"
            else -> "webm"
        }
    }
}
