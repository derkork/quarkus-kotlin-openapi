package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

interface KotlinStatement {

    fun render(writer: CodeWriter)

}

interface StatementAware {

    fun addStatement(statement: KotlinStatement)

}

fun StatementAware.kotlinStatement(block: CodeWriter.() -> Unit) {
    val content = object : KotlinStatement {
        override fun render(writer: CodeWriter) {
            writer.block()
        }
    }
    addStatement(content)
}

