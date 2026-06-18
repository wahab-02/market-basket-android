package ai.algo1.marketbasket.core.domain

import ai.algo1.marketbasket.core.domain.util.AudioMimeTypes.mimeTypeToExtension
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioMimeTypesTest {
    @Test fun knownTypes() {
        assertEquals("webm", mimeTypeToExtension("audio/webm"))
        assertEquals("ogg", mimeTypeToExtension("audio/ogg"))
        assertEquals("mp4", mimeTypeToExtension("audio/mp4"))
        assertEquals("wav", mimeTypeToExtension("audio/wav"))
        assertEquals("wav", mimeTypeToExtension("audio/wave"))
        assertEquals("mp3", mimeTypeToExtension("audio/mpeg"))
    }

    @Test fun stripsCodecsParameter() {
        assertEquals("webm", mimeTypeToExtension("audio/webm;codecs=opus"))
    }

    @Test fun unknownDefaultsToWebm() {
        assertEquals("webm", mimeTypeToExtension("audio/x-flac"))
    }
}
