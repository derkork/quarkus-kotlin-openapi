package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinStatementContainer {

    private val statements = mutableListOf<KotlinStatement>()

    fun addStatement(statement: KotlinStatement) {
        statements.add(statement)
    }

    val isNotEmpty: Boolean
        get() = statements.isNotEmpty()

    fun render(writer: CodeWriter) = with(writer) {
        statements.forEachWithStats { status, statement ->
            statement.render(this)
            if (!status.last) {
                writeln(forceNewLine = false)
            }
        }
    }
}