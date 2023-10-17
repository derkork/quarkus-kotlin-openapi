package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class KotlinParameter(
    private val name: VariableName, private val type: TypeName,
    private val expression: Expression? = null
) : AnnotationAware {

    private val annotations = KotlinAnnotationContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)
        write("${name.render()}: ${type.render()}")
        if (expression != null) {
            write(" = ${expression.evaluate()}")
        }
    }

}

interface ParameterAware {

    fun addParameter(parameter: KotlinParameter)

}

fun ParameterAware.kotlinParameter(
    name: VariableName,
    type: TypeName,
    expression: Expression? = null,
    block: KotlinParameter.() -> Unit = {}
) {
    val content = KotlinParameter(name, type, expression).apply(block)
    addParameter(content)

}