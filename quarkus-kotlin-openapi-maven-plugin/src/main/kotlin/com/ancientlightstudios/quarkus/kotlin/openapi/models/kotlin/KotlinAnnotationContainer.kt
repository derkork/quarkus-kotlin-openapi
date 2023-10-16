package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinAnnotationContainer {

    private val annotations = mutableListOf<KotlinAnnotation>()

    fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.add(annotation)
    }

    fun render(writer: CodeWriter, onSingleLine: Boolean = false) = with(writer) {
        annotations.forEach {
            it.render(this)
            if (onSingleLine) {
                write(" ")
            } else {
                writeln()
            }
        }
    }

}
