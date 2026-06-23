package com.screenlink.tv.core.network.api

import java.io.IOException

class HttpStatusException(val statusCode: Int, message: String) : IOException(message)
