package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinCompanion(private val identifier: KotlinTypeName? = null, private val keepIfEmpty: Boolean = false) :
    KotlinRenderable, MethodAware, CommentAware {

    private val methods = KotlinRenderableBlockContainer<KotlinMethod>()
    private var comment: KotlinComment? = null

    override fun addMethod(method: KotlinMethod) {
        methods.addItem(method)
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }

    override fun ImportCollector.registerImports() {
        registerFrom(methods)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        if (!keepIfEmpty && methods.isEmpty) {
            return
        }

        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        write("companion object ")
        if (identifier != null) {
            write("${identifier.name} ")
        }
        block {
            writeln()
            methods.render(this)
            writeln(forceNewLine = false) // in case the item already rendered a line break
            writeln()
        }
    }
}

interface CompanionAware {

    fun setCompanion(companion: KotlinCompanion)

}

fun CompanionAware.kotlinCompanion(
    identifier: KotlinTypeName? = null, keepIfEmpty: Boolean = false, block: KotlinCompanion.() -> Unit
) = KotlinCompanion(identifier).apply(block).also { setCompanion(it) }
