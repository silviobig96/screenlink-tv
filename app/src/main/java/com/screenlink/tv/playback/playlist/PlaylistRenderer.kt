package com.screenlink.tv.playback.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.screenlink.tv.domain.commands.models.PlaylistItem
import com.screenlink.tv.playback.image.FullscreenImage
import com.screenlink.tv.playback.video.FullscreenVideo
import kotlinx.coroutines.delay

@Composable
fun PlaylistRenderer(items: List<PlaylistItem>, loop: Boolean, onReady: () -> Unit, onError: (String) -> Unit) {
    var index by remember(items) { mutableIntStateOf(0) }
    var imageReady by remember(index) { mutableStateOf(false) }
    val item = items.getOrNull(index) ?: return

    fun advance() {
        index = when {
            index < items.lastIndex -> index + 1
            loop -> 0
            else -> index
        }
    }

    LaunchedEffect(index, imageReady) {
        val image = item as? PlaylistItem.Image
        if (image != null && imageReady) {
            delay(image.durationMs)
            advance()
        }
    }
    LaunchedEffect(index, item) {
        val video = item as? PlaylistItem.Video
        video?.durationMs?.let {
            delay(it)
            advance()
        }
    }

    when (item) {
        is PlaylistItem.Image -> FullscreenImage(
            url = item.url,
            onReady = {
                imageReady = true
                onReady()
            },
            onError = onError,
        )
        is PlaylistItem.Video -> FullscreenVideo(
            url = item.url,
            onReady = onReady,
            onCompleted = ::advance,
            onError = onError,
        )
    }
}
