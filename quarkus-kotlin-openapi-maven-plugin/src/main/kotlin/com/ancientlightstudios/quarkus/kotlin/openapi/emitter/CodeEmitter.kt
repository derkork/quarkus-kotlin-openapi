package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite

interface CodeEmitter {

    fun EmitterContext.emit(suite: RequestSuite)

}