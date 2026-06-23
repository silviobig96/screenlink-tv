package com.screenlink.tv.playback.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.screenlink.tv.core.network.media.MediaDiagnostics
import com.screenlink.tv.core.network.media.MediaEntryPoint
import com.screenlink.tv.core.network.media.MediaHeaders
import dagger.hilt.android.EntryPointAccessors

@Composable
fun FullscreenImage(url: String, onReady: () -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current
    val entryPoint = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, MediaEntryPoint::class.java)
    }
    val request = remember(url, context) {
        ImageRequest.Builder(context)
            .data(url)
            .addHeader("Accept", MediaHeaders.IMAGE_ACCEPT)
            .build()
    }
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = request,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            onSuccess = { onReady() },
            onError = {
                entryPoint.logger().error(MediaDiagnostics.describeImageFailure(url, it.result.throwable), it.result.throwable)
                onError(IMAGE_ERROR_MESSAGE)
            },
        )
    }
}

private const val IMAGE_ERROR_MESSAGE = "Image could not be loaded. The URL may block TV clients or return an unsupported format."
