package com.ancientlightstudios.quarkus.kotlin.openapi.strafbank

import java.io.BufferedWriter

class CodeWriter private constructor(private val level: Int, private val writer: BufferedWriter) {

    constructor(writer: BufferedWriter) : this(0, writer)

    fun write(text: String) {
        writer.write(text.trimIndent().prependIndent("    ".repeat(level)))
    }

    fun writeln(text: String) {
        write(text)
        writer.newLine()
    }

    fun writeln() {
        writer.newLine()
    }

    fun indent(block: CodeWriter.() -> Unit) {
        block(CodeWriter(level + 1, writer))
    }

}