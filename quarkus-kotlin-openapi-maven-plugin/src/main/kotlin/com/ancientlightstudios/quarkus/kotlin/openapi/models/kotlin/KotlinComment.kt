package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinComment(private val blockComment: Boolean = false) : KotlinRenderable {

    private val lines = mutableListOf<String>()

    fun addLine(line: String) {
        lines.add(line)
    }

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        val prefix = when (blockComment) {
            true -> " *"
            else -> "//"
        }

        if (blockComment) {
            writeln("/**")
        }

        lines.forEachWithStats { stats, item ->
            write("$prefix $item")
            if (!stats.last) {
                writeln(forceNewLine = false)
            }
        }

        if (blockComment) {
            writeln(forceNewLine = false)
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
