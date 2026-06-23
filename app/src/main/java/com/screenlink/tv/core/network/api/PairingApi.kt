package com.screenlink.tv.core.network.api

import com.screenlink.tv.core.config.AppConfig
import com.screenlink.tv.core.network.dto.PairingRequestDto
import com.screenlink.tv.core.network.dto.PairingResponseDto
import com.screenlink.tv.core.network.dto.PairingStatusDto
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PairingApi @Inject constructor(private val client: OkHttpClient, private val json: Json, private val config: AppConfig) {
    suspend fun requestPairing(request: PairingRequestDto): PairingResponseDto {
        val body = json.encodeToString(request).toRequestBody(JSON_MEDIA_TYPE)
        val httpRequest = Request.Builder().url(config.apiBaseUrl + "pairing/request").post(body).build()
        return json.decodeFromString(client.execute(httpRequest))
    }

    suspend fun getPairingStatus(screenId: String): PairingStatusDto {
        val httpRequest = Request.Builder().url(config.apiBaseUrl + "pairing/status/$screenId").get().build()
        return json.decodeFromString(client.execute(httpRequest))
    }

    private suspend fun OkHttpClient.execute(request: Request): String = suspendCancellableCoroutine { continuation ->
        val call = newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, error: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(error)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val body = it.body?.string().orEmpty()
                        if (it.isSuccessful) {
                            continuation.resume(body)
                        } else {
                            continuation.resumeWithException(HttpStatusException(it.code, "HTTP ${it.code}"))
                        }
                    }
                }
            },
        )
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
