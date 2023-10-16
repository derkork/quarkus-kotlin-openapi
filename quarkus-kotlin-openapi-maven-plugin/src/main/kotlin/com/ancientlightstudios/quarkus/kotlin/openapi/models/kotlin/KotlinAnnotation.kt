package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinAnnotation(private val name: ClassName, private vararg val parameters: Pair<VariableName, Expression>) {

    fun render(writer: CodeWriter) = with(writer) {
        write("@${name.render()}")

        if (parameters.isNotEmpty()) {
            write("(")
            parameters.forEachWithStats { status, (name, value) ->
                write("${name.render()} = ${value.evaluate()}")
                if (!status.last) {
                    write(", ")
                }
            }
            write(")")
        }
    }
}

interface AnnotationAware {

    fun addAnnotation(annotation: KotlinAnnotation)

}

fun AnnotationAware.kotlinAnnotation(name: ClassName, vararg parameters: Pair<VariableName, Expression>) {
    addAnnotation(KotlinAnnotation(name, *parameters))
}
