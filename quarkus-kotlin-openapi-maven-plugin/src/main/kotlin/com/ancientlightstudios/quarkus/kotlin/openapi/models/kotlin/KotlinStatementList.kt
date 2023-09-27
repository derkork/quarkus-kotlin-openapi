package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinStatementList {

    val statements = mutableListOf<KotlinStatement>()

    fun render(writer: CodeWriter) = with(writer) {
        statements.forEachWithStats { status, statement ->
            statement.render(this)
            if (!status.last) {
                writeln(forceNewLine = false)
            }
        }
    }
}