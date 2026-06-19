package com.screenlink.tv.domain.commands.models

sealed interface ScreenCommand {
    val id: String

    data class DisplayImage(override val id: String, val url: String) : ScreenCommand

    data class DisplayVideo(override val id: String, val url: String) : ScreenCommand

    data class PlayPlaylist(override val id: String, val items: List<PlaylistItem>, val loop: Boolean = true) : ScreenCommand

    data class ClearScreen(override val id: String) : ScreenCommand

    data class SyncContent(override val id: String) : ScreenCommand

    data class Ping(override val id: String) : ScreenCommand

    data class Unsupported(override val id: String, val type: String) : ScreenCommand

    data class Invalid(override val id: String, val reason: String) : ScreenCommand
}

sealed interface PlaylistItem {
    data class Image(val url: String, val durationMs: Long) : PlaylistItem

    data class Video(val url: String, val durationMs: Long? = null) : PlaylistItem
}
