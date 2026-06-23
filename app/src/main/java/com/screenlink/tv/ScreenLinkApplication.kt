package com.screenlink.tv

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.screenlink.tv.core.network.media.MediaHttpClient
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class ScreenLinkApplication :
    Application(),
    ImageLoaderFactory {
    @Inject
    @MediaHttpClient
    lateinit var mediaHttpClient: OkHttpClient

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient(mediaHttpClient)
        .build()
}
