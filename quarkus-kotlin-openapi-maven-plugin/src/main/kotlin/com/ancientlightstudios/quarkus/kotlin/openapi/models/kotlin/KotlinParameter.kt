package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinParameter(private val name: VariableName, private val type: TypeName) : AnnotationAware {

    private val annotations = KotlinAnnotationContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)
        write("${name.render()}: ${type.render()}")
    }

}

interface ParameterAware {

    fun addParameter(parameter: KotlinParameter)

}

fun ParameterAware.kotlinParameter(name: VariableName, type: TypeName, block: KotlinParameter.() -> Unit = {}) {
    val content = KotlinParameter(name, type).apply(block)
    addParameter(content)

}