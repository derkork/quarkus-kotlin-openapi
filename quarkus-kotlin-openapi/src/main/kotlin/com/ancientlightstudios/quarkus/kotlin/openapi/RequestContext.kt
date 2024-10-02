package com.ancientlightstudios.quarkus.kotlin.openapi

interface RequestContext {

    fun rawHeaderValue(name: String): String?

    fun rawHeaderValues(name: String): List<String>

}