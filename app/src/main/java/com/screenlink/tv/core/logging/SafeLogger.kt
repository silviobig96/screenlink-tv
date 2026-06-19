package com.screenlink.tv.core.logging

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

interface SafeLogger {
    fun info(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

@Singleton
class AndroidSafeLogger @Inject constructor() : SafeLogger {
    override fun info(message: String) {
        Log.i(TAG, message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Log.e(TAG, message, throwable)
    }

    private companion object {
        const val TAG = "ScreenLink"
    }
}
