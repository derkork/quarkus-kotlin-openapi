package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

interface KotlinRenderable {

    fun ImportCollector.registerImports()

    fun render(writer: CodeWriter)

}
