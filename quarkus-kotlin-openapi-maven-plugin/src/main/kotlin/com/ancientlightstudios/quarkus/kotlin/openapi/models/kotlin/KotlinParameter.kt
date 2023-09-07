package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinParameter(private val name: VariableName, private val type: TypeName) {

    val annotations = KotlinAnnotationContainer()

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, false)
        write("${name.name}: ")
        type.render(this)
    }

}
