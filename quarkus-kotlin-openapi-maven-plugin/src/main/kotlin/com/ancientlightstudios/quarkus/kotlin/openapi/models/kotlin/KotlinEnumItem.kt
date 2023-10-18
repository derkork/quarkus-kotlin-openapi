package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ConstantName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinEnumItem(private val name: ConstantName, private vararg val values: Expression) : AnnotationAware {

    private val annotations = KotlinAnnotationContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this)

        write(name.render())
        if (values.isNotEmpty()) {
            write("(")
            values.forEachWithStats { status, value ->
                write(value.evaluate())
                if (!status.last) {
                    write(", ")
                }
            }
            write(")")
        }
    }

}

fun KotlinEnum.kotlinEnumItem(name: ConstantName, vararg values: Expression, block: KotlinEnumItem.() -> Unit = {}) {
    val content = KotlinEnumItem(name, *values).apply(block)
    addItem(content)
}
