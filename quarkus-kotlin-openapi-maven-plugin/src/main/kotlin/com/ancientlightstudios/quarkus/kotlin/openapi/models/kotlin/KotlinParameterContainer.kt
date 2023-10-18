package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.renderWithWrap
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinParameterContainer {

    private val parameters = mutableListOf<KotlinParameter>()

    fun addParameter(parameter: KotlinParameter) {
        parameters.add(parameter)
    }

    fun render(writer: CodeWriter) = with(writer) {
        renderWithWrap(parameters, 2) { it.render(this) }
    }
}