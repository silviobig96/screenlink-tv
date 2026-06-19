package com.screenlink.tv.core.network

import com.screenlink.tv.core.network.dto.PairingResponseDto
import com.screenlink.tv.core.network.dto.PairingStatusDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PairingDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `pairing request response parses`() {
        val result = json.decodeFromString<PairingResponseDto>(
            """{"screenId":"screen-1","pairingCode":"123456","expiresAt":"2026-06-19T12:00:00Z"}""",
        )
        assertEquals("screen-1", result.screenId)
        assertEquals("123456", result.pairingCode)
    }

    @Test
    fun `pending pairing status parses`() {
        val result = json.decodeFromString<PairingStatusDto>("""{"status":"pending"}""")
        assertEquals("pending", result.status)
        assertNull(result.deviceToken)
    }

    @Test
    fun `paired status with token parses`() {
        val result = json.decodeFromString<PairingStatusDto>("""{"status":"paired","deviceToken":"secret"}""")
        assertEquals("secret", result.deviceToken)
    }

    @Test
    fun `paired status without token parses`() {
        val result = json.decodeFromString<PairingStatusDto>("""{"status":"paired"}""")
        assertEquals("paired", result.status)
        assertNull(result.deviceToken)
    }
}
