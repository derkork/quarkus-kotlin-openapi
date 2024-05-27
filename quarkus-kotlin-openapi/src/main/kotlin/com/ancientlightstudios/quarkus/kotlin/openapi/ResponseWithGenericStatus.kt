package com.ancientlightstudios.quarkus.kotlin.openapi

interface ResponseWithGenericStatus {

    fun status(status: Int, mediaType: String? = null, body: Any? = null, vararg headers: Pair<String, Any?>): Nothing

}