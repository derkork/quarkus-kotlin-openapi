package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName

class KotlinValueClass(
    private val name: ClassName, private val nestedType: TypeName, private val override: Boolean = false,
    private val extends: List<ExtendExpression> = emptyList()
) : KotlinRenderable,
    AnnotationAware, MethodAware, CommentAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinRenderableBlockContainer<KotlinMethod>()
    private var comment: KotlinComment? = null

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addItem(method)
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }

    override fun render(writer: CodeWriter) = with(writer) {
        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        annotations.render(this)
        write("value class ${name.render()}(")
        if (override) {
            write("override ")
        }
        write("val value: ${nestedType.render()})")

        if (extends.isNotEmpty()) {
            write(extends.joinToString(prefix = " : ") { it.evaluate() })
        }

        if (methods.isNotEmpty) {
            writeln(" {")
            indent {
                writeln()
                methods.render(this)
                writeln(forceNewLine = false) // in case the item already rendered a line break
                writeln()
            }
            write("}")
        }

        // companion
    }

}

interface ValueClassAware {

    fun addValueClass(valueClass: KotlinValueClass)

}

fun ValueClassAware.kotlinValueClass(
    name: ClassName, nestedType: TypeName, override: Boolean = false,
    extends: List<ExtendExpression> = emptyList(), block: KotlinValueClass.() -> Unit = {}
) {
    val content = KotlinValueClass(name, nestedType, override, extends).apply(block)
    addValueClass(content)
}
