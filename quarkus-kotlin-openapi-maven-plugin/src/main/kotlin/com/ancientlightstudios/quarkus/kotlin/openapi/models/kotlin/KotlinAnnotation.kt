package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinAnnotation(
    private val name: ClassName,
    private vararg val parameters: Pair<VariableName?, KotlinExpression>
) : KotlinRenderable {

    override fun ImportCollector.registerImports() {
        register(name)
        registerFrom(parameters.map { (_, expression) -> expression })
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("@${name.value}")

        if (parameters.isNotEmpty()) {
            write("(")
            parameters.forEachWithStats { status, (name, value) ->
                name?.let { write("${it.value} = ") }
                value.render(this)
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

fun AnnotationAware.kotlinAnnotation(name: ClassName, vararg parameters: Pair<VariableName, KotlinExpression>) {
    addAnnotation(KotlinAnnotation(name, *parameters))
}
