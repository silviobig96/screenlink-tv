package com.screenlink.tv.core.network.media

import androidx.media3.common.PlaybackException
import androidx.media3.datasource.HttpDataSource
import okhttp3.Response
import java.net.URI

object MediaDiagnostics {
    fun describeImageFailure(url: String, throwable: Throwable?): String = buildString {
        append("Image load failed")
        appendCommon(url, throwable)
        val response = throwable?.causeChain()?.firstNotNullOfOrNull(::extractOkHttpResponse)
        response?.let {
            append("; httpStatus=").append(it.code)
            it.header("Content-Type")?.let { contentType -> append("; contentType=").append(contentType) }
        }
    }

    fun describeVideoFailure(url: String, error: PlaybackException): String = buildString {
        append("Video playback failed")
        appendCommon(url, error)
        val httpError = error.causeChain().filterIsInstance<HttpDataSource.InvalidResponseCodeException>().firstOrNull()
        httpError?.let {
            append("; httpStatus=").append(it.responseCode)
        }
    }

    fun hostOnly(url: String): String = runCatching {
        URI(url).host ?: "unknown"
    }.getOrDefault("unknown")

    private fun StringBuilder.appendCommon(url: String, throwable: Throwable?) {
        append("; host=").append(hostOnly(url))
        throwable?.let {
            append("; exception=").append(it::class.java.simpleName)
            it.message?.take(160)?.let { message -> append("; message=").append(message) }
        }
    }

    private fun Throwable.causeChain(): Sequence<Throwable> = generateSequence(this) { it.cause }

    private fun extractOkHttpResponse(throwable: Throwable): Response? = runCatching {
        val responseMethod = throwable.javaClass.methods.firstOrNull { it.name == "response" && it.parameterCount == 0 }
        responseMethod?.invoke(throwable) as? Response
    }.getOrNull()
}
