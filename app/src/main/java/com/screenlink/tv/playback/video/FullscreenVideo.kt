package com.screenlink.tv.playback.video

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.screenlink.tv.core.network.media.MediaDiagnostics
import com.screenlink.tv.core.network.media.MediaEntryPoint
import com.screenlink.tv.core.network.media.MediaHeaders
import dagger.hilt.android.EntryPointAccessors

@OptIn(UnstableApi::class)
@Composable
fun FullscreenVideo(url: String, onReady: () -> Unit, onCompleted: () -> Unit = {}, onError: (String) -> Unit) {
    val context = LocalContext.current
    val entryPoint = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, MediaEntryPoint::class.java)
    }
    val player = remember(url) {
        val dataSourceFactory = OkHttpDataSource.Factory(entryPoint.mediaHttpClient())
            .setUserAgent(MediaHeaders.userAgent(entryPoint.appConfig()))
            .setDefaultRequestProperties(mapOf("Accept" to MediaHeaders.VIDEO_ACCEPT))
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(url))
                playWhenReady = true
                prepare()
            }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> onReady()
                    Player.STATE_ENDED -> onCompleted()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                entryPoint.logger().error(MediaDiagnostics.describeVideoFailure(url, error), error)
                onError(VIDEO_ERROR_MESSAGE)
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    useController = false
                    this.player = player
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private const val VIDEO_ERROR_MESSAGE =
    "Video could not be played. The URL may block TV clients, be unavailable, or use an unsupported codec."
