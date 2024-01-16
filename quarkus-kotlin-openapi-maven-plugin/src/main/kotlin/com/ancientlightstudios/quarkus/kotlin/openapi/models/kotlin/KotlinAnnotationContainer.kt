package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinAnnotationContainer(private val onSingleLine: Boolean = false) : KotlinRenderable {

    private val annotations = mutableListOf<KotlinAnnotation>()

    fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.add(annotation)
    }

    override fun ImportCollector.registerImports() {
        registerFrom(annotations)
    }

    override fun render(writer: CodeWriter) = with(writer) {
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
