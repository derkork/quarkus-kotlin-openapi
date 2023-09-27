package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

interface KotlinStatement {

    fun render(writer: CodeWriter)

}

fun kotlinStatement(block: CodeWriter.() -> Unit) = object : KotlinStatement {
    override fun render(writer: CodeWriter) {
        writer.block()
    }
}

fun String.asKotlinStatement() = kotlinStatement { write(this@asKotlinStatement) }

fun KotlinStatement.then(other: KotlinStatement) = kotlinStatement {
    this@then.render(this)
    other.render(this)
} 