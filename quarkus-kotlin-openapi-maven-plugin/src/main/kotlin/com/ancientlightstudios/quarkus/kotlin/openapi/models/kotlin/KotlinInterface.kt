package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendFromInterfaceExpression

class KotlinInterface(
    private val name: ClassName,
    val sealed: Boolean = false,
    private val extends: List<ExtendFromInterfaceExpression> = emptyList()
) : KotlinRenderable, AnnotationAware, MethodAware, CommentAware, ClassAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinRenderableBlockContainer<KotlinMethod>()
    private var comment: KotlinComment? = null
    private val items = KotlinRenderableBlockContainer<KotlinRenderable>()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addItem(method)
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }


    override fun addClass(clazz: KotlinClass) {
        items.addItem(clazz)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        annotations.render(this, false)
        if (sealed) {
            write("sealed ")
        }
        write("interface ${name.render()}")

        if (extends.isNotEmpty()) {
            write(extends.joinToString(prefix = " : ") { it.evaluate() })
        }

        if (methods.isNotEmpty || items.isNotEmpty) {
            write(" ")
            block {
                if (items.isNotEmpty) {
                    writeln()
                    items.render(this)
                    writeln(forceNewLine = false)
                }

                if (methods.isNotEmpty) {
                    writeln()
                    methods.render(this)
                    writeln(forceNewLine = false)
                }
            }
        }
    }

}

interface InterfaceAware {

    fun addInterface(interfaze: KotlinInterface)

}

fun InterfaceAware.kotlinInterface(
    name: ClassName,
    sealed: Boolean = false,
    extends: List<ExtendFromInterfaceExpression> = emptyList(),
    block: KotlinInterface.() -> Unit
) {
    val content = KotlinInterface(name, sealed, extends).apply(block)
    addInterface(content)
}
