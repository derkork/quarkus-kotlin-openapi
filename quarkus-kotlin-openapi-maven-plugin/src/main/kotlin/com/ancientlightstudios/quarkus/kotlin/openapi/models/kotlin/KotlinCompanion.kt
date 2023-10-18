package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinCompanion(private val identifier: ClassName? = null) : MethodAware, CommentAware {

    private val methods = KotlinMethodContainer()
    private var comment: KotlinComment? = null

    override fun addMethod(method: KotlinMethod) {
        methods.addMethod(method)
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }

    fun render(writer: CodeWriter) = with(writer) {
        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        write("companion object ")
        if (identifier != null) {
            write("${identifier.render()} ")
        }
        writeln("{")
        indent {
            writeln()
            methods.render(this)
            writeln(forceNewLine = false) // in case the item already rendered a line break
            writeln()
        }
        writeln("}")
    }
}

interface CompanionAware {

    fun setCompanion(companion: KotlinCompanion)

}

fun CompanionAware.kotlinCompanion(identifier: ClassName? = null, block: KotlinCompanion.() -> Unit) {
    val content = KotlinCompanion(identifier).apply(block)
    setCompanion(content)
}
