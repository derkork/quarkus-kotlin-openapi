package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinParameter(val name:Name.VariableName, val type:Name.ClassName) {

        val annotations = KotlinAnnotationContainer()

        fun render(writer: CodeWriter) {
            annotations.render(writer, false)
            writer.write("${name.name}: ${type.name}")
        }

}
