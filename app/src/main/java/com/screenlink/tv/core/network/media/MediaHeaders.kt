package com.screenlink.tv.core.network.media

import com.screenlink.tv.core.config.AppConfig

object MediaHeaders {
    const val IMAGE_ACCEPT = "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"
    const val VIDEO_ACCEPT = "video/mp4,video/webm,video/*,*/*;q=0.8"

    fun userAgent(config: AppConfig): String = "ScreenLinkTV/${config.appVersion} AndroidTV"
}
