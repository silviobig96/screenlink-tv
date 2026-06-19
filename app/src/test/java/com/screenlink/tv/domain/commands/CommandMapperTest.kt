package com.screenlink.tv.domain.commands

import com.screenlink.tv.data.commands.mappers.CommandMapper
import com.screenlink.tv.domain.commands.models.ScreenCommand
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandMapperTest {
    private val mapper = CommandMapper(Json { ignoreUnknownKeys = true })

    @Test
    fun `display image command parses`() {
        val command = mapper.map(
            """{"commandId":"c1","type":"DISPLAY_IMAGE","payload":{"url":"https://example.com/a.jpg"}}""",
        )
        assertEquals("https://example.com/a.jpg", (command as ScreenCommand.DisplayImage).url)
    }

    @Test
    fun `unsupported command is safe`() {
        val command = mapper.map("""{"commandId":"c2","type":"REBOOT","payload":{}}""")
        assertTrue(command is ScreenCommand.Unsupported)
        assertEquals("REBOOT", (command as ScreenCommand.Unsupported).type)
    }

    @Test
    fun `missing media url is invalid`() {
        val command = mapper.map("""{"commandId":"c3","type":"DISPLAY_VIDEO","payload":{}}""")
        assertTrue(command is ScreenCommand.Invalid)
    }

    @Test
    fun `playlist event maps playable items`() {
        val command = mapper.map(
            """{"id":"c4","type":"PLAY_PLAYLIST","payload":{
                "items":[{"type":"IMAGE","url":"https://example.com/a.jpg","durationMs":5000}]
            }}
            """.trimIndent(),
        )
        assertEquals(1, (command as ScreenCommand.PlayPlaylist).items.size)
    }
}
