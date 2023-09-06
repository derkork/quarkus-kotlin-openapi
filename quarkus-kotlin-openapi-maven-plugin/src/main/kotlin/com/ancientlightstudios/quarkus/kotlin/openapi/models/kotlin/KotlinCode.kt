package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinCode(private val code: String) {

    fun render(writer: CodeWriter) = with(writer) {
        writeln(code)
    }
}