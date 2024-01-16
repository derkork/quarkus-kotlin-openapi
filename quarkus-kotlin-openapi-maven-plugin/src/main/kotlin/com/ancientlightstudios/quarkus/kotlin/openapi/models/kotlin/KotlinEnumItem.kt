package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinEnumItem(private val name: ConstantName, private vararg val values: KotlinExpression) :
    KotlinRenderable, AnnotationAware {

    private val annotations = KotlinAnnotationContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun ImportCollector.registerImports() {
        registerFrom(values.toList())
    }

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this)

        write(name.value)
        if (values.isNotEmpty()) {
            write("(")
            values.forEachWithStats { status, value ->
                value.render(this)
                if (!status.last) {
                    write(", ")
                }
            }
            write(")")
        }
    }

}

fun KotlinEnum.kotlinEnumItem(
    name: ConstantName,
    vararg values: KotlinExpression,
    block: KotlinEnumItem.() -> Unit = {}
) {
    val content = KotlinEnumItem(name, *values).apply(block)
    addItem(content)
}
