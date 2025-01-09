package com.ancientlightstudios.quarkus.kotlin.openapi.handler

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType

interface Handler {

    fun initializeContext(registry: HandlerRegistry) {
        // does nothing by default
    }

}