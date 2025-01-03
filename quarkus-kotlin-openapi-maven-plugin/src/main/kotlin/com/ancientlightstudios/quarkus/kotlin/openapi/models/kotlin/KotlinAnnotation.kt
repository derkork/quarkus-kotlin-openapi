package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinAnnotation(
    private val name: KotlinTypeName,
    private vararg val parameters: Pair<String?, KotlinExpression>
) : KotlinRenderable {

    override fun ImportCollector.registerImports() {
        register(name)
        registerFrom(parameters.map { (_, expression) -> expression })
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("@${name.name}")

        if (parameters.isNotEmpty()) {
            write("(")
            parameters.forEachWithStats { status, (name, value) ->
                name?.let { write("$it = ") }
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

fun AnnotationAware.kotlinAnnotation(name: KotlinTypeName, vararg parameters: Pair<String, KotlinExpression>) {
    addAnnotation(KotlinAnnotation(name, *parameters))
}

fun AnnotationAware.kotlinAnnotation(name: KotlinTypeName, parameter: KotlinExpression) {
    addAnnotation(KotlinAnnotation(name, null to parameter))
}
