package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinComment(private val blockComment: Boolean = false) {

    private val lines = mutableListOf<String>()

    fun addLine(line: String) {
        lines.add(line)
    }

    fun render(writer: CodeWriter) = with(writer) {
        val prefix = when (blockComment) {
            true -> " *"
            else -> "//"
        }

        if (blockComment) {
            writeln("/**")
        }

        lines.forEach {
            write("$prefix $it")
        }

        if (blockComment) {
            write(" */")
        }
    }
}

interface CommentAware {

    fun setComment(comment: KotlinComment)

}

fun CommentAware.kotlinComment(blockComment: Boolean = false, block: KotlinComment.() -> Unit) {
    val content = KotlinComment(blockComment).apply(block)
    setComment(content)
}
