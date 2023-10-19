package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinInterface(private val name: ClassName) : KotlinRenderable, AnnotationAware, MethodAware, CommentAware {

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

        annotations.render(this, false)
        write("interface ${name.render()}")

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
    }

}

interface InterfaceAware {

    fun addInterface(interfaze: KotlinInterface)

}

fun InterfaceAware.kotlinInterface(name: ClassName, block: KotlinInterface.() -> Unit) {
    val content = KotlinInterface(name).apply(block)
    addInterface(content)
}
