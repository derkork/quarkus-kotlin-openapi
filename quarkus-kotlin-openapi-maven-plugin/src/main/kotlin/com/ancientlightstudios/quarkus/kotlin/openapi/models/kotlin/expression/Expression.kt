package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinRenderable

interface Expression : KotlinRenderable {

    fun evaluate(): String

    override fun render(writer: CodeWriter) = with(writer) {
        write(evaluate())
    }

}