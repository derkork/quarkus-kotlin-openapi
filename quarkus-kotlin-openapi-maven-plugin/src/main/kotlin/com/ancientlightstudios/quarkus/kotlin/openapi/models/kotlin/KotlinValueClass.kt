package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName

class KotlinValueClass(private val name: ClassName, private val nestedType: TypeName) : KotlinRenderable,
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
        write("value class ${name.render()}(val value: ${nestedType.render()})")

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

fun ValueClassAware.kotlinValueClass(name: ClassName, nestedType: TypeName, block: KotlinValueClass.() -> Unit = {}) {
    val content = KotlinValueClass(name, nestedType).apply(block)
    addValueClass(content)
}
