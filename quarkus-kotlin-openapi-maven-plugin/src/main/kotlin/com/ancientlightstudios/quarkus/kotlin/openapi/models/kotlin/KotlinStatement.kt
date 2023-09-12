package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

interface KotlinStatement {

    fun render(writer: CodeWriter)

}

fun kotlinStatement(block: CodeWriter.() -> Unit): KotlinStatement = object : KotlinStatement {
    override fun render(writer: CodeWriter) {
        writer.block()
    }
}