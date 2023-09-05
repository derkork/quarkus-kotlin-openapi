package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import java.io.BufferedWriter

class CodeWriter private constructor(private val level: Int, private val writer: BufferedWriter) {

    constructor(writer: BufferedWriter) : this(0, writer)

    private var onEmptyLine = true

    fun write(text: String) {
        if (onEmptyLine) {
            writer.write(text.trimIndent().prependIndent("    ".repeat(level)))
        } else {
            writer.write(text)
        }
        onEmptyLine = false
    }

    fun writeln(text: String) {
        write(text)
        writer.newLine()
        onEmptyLine = true
    }

    fun writeln() {
        writer.newLine()
        onEmptyLine = true
    }

    fun indent(block: CodeWriter.() -> Unit) {
        block(CodeWriter(level + 1, writer))
    }

}