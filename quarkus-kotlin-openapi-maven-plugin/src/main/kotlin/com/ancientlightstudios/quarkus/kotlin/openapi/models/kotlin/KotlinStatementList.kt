package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinStatementList {

    val statements = mutableListOf<KotlinStatement>()

    fun render(writer: CodeWriter) {
        statements.forEach {
            it.render(writer)
        }
    }
}