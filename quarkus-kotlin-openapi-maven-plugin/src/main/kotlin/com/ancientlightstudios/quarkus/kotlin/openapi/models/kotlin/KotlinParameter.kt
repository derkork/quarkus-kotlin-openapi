package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinParameter(
    private val name: VariableName, private val type: TypeName,
    private val expression: KotlinExpression? = null,
    private val asParameterList : Boolean = false
) : KotlinRenderable, AnnotationAware {

    private val annotations = KotlinAnnotationContainer(true)

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun ImportCollector.registerImports() {
        register(type)
        expression?.let { registerFrom(expression) }
        registerFrom(annotations)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this)
        if (asParameterList) {
            write("vararg ")
        }
        write("${name.value}: ${type.value}")
        if (expression != null) {
            write(" = ")
            expression.render(this)
        }
    }

}

interface ParameterAware {

    fun addParameter(parameter: KotlinParameter)

}

fun ParameterAware.kotlinParameter(
    name: VariableName,
    type: TypeName,
    expression: KotlinExpression? = null,
    asParameterList : Boolean = false,
    block: KotlinParameter.() -> Unit = {}
) {
    val content = KotlinParameter(name, type, expression, asParameterList).apply(block)
    addParameter(content)

}