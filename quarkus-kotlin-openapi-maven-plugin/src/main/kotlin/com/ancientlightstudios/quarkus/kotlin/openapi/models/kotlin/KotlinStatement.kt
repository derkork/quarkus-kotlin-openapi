package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

interface KotlinStatement : KotlinRenderable

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

