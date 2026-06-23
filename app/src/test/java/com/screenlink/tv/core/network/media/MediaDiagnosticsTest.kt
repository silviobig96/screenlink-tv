package com.screenlink.tv.core.network.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaDiagnosticsTest {
    @Test
    fun `hostOnly strips path and query from signed URLs`() {
        val url = "https://cdn.example.com/path/image.jpg?signature=secret-token"

        assertEquals("cdn.example.com", MediaDiagnostics.hostOnly(url))
    }

    @Test
    fun `image diagnostics do not include full signed URL`() {
        val url = "https://cdn.example.com/path/image.jpg?signature=secret-token"

        val message = MediaDiagnostics.describeImageFailure(url, IllegalStateException("boom"))

        assertTrue(message.contains("host=cdn.example.com"))
        assertFalse(message.contains("signature=secret-token"))
        assertFalse(message.contains("/path/image.jpg"))
    }
}
