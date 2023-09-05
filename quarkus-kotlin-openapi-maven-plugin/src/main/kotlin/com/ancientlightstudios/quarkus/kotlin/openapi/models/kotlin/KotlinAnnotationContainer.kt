package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinAnnotationContainer {
    val annotations: MutableList<KotlinAnnotation> = mutableListOf()

    fun add(name: Name.ClassName, vararg parameters: Pair<Name.VariableName, Any>) {
        annotations.add(KotlinAnnotation(name, *parameters))
    }

    fun render(writer: CodeWriter, addNewLines: Boolean) {
        annotations.forEach {
            it.render(writer)
            if (addNewLines) {
                writer.writeln()
            } else {
                writer.write(" ")
            }
        }
    }
}