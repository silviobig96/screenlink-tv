package com.screenlink.tv.data.commands.mappers

import com.screenlink.tv.domain.commands.models.PlaylistItem
import com.screenlink.tv.domain.commands.models.ScreenCommand
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.json.JSONObject
import javax.inject.Inject

class CommandMapper @Inject constructor(private val json: Json) {
    fun map(raw: Any): ScreenCommand = runCatching {
        val root = when (raw) {
            is JSONObject -> json.parseToJsonElement(raw.toString()).jsonObject
            is String -> json.parseToJsonElement(raw).jsonObject
            else -> error("Unsupported event payload")
        }
        val id = root.string("commandId") ?: root.string("id") ?: "unknown"
        val type = root.string("type") ?: return ScreenCommand.Invalid(id, "Command type is missing")
        val payload = root["payload"] as? JsonObject ?: JsonObject(emptyMap())
        mapByType(id, type, payload)
    }.getOrElse { ScreenCommand.Invalid("unknown", "Invalid command payload") }

    private fun mapByType(id: String, type: String, payload: JsonObject): ScreenCommand = when (type) {
        "DISPLAY_IMAGE" -> payload.requiredUrl()?.let { ScreenCommand.DisplayImage(id, it) }
            ?: ScreenCommand.Invalid(id, "Image URL is missing")
        "DISPLAY_VIDEO" -> payload.requiredUrl()?.let { ScreenCommand.DisplayVideo(id, it) }
            ?: ScreenCommand.Invalid(id, "Video URL is missing")
        "PLAY_PLAYLIST" -> mapPlaylist(id, payload)
        "CLEAR_SCREEN" -> ScreenCommand.ClearScreen(id)
        "SYNC_CONTENT" -> ScreenCommand.SyncContent(id)
        "PING" -> ScreenCommand.Ping(id)
        else -> ScreenCommand.Unsupported(id, type)
    }

    private fun mapPlaylist(id: String, payload: JsonObject): ScreenCommand {
        val rawItems = payload["items"] as? JsonArray ?: return ScreenCommand.Invalid(id, "Playlist items are missing")
        val items = rawItems.mapNotNull { element ->
            val item = element.jsonObject
            val url = item.string("url") ?: return@mapNotNull null
            when (item.string("type")) {
                "IMAGE" -> PlaylistItem.Image(url, item.long("durationMs") ?: DEFAULT_IMAGE_DURATION_MS)
                "VIDEO" -> PlaylistItem.Video(url, item.long("durationMs"))
                else -> null
            }
        }
        return if (items.isEmpty()) {
            ScreenCommand.Invalid(id, "Playlist has no playable items")
        } else {
            ScreenCommand.PlayPlaylist(id, items, payload["loop"]?.jsonPrimitive?.booleanOrNull ?: true)
        }
    }

    private fun JsonObject.requiredUrl(): String? = string("url")?.takeIf { it.startsWith("http://") || it.startsWith("https://") }

    private fun JsonObject.string(key: String) = this[key]?.jsonPrimitive?.contentOrNull

    private fun JsonObject.long(key: String) = this[key]?.jsonPrimitive?.longOrNull

    private companion object {
        const val DEFAULT_IMAGE_DURATION_MS = 10_000L
    }
}
