package com.ancientlightstudios.quarkus.kotlin.openapi

interface RequestContext {

    val requestMethod: String

    val requestPath: String

    fun rawHeaderValue(name: String): String?

    fun rawHeaderValues(name: String): List<String>

}