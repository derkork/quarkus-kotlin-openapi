package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import java.io.BufferedWriter

class CodeWriter private constructor(private val level: Int, private val writer: BufferedWriter) {

    constructor(writer: BufferedWriter) : this(0, writer)

    private var onEmptyLine = true

    fun write(text: String, newLineBefore: Boolean = false, newLineAfter: Boolean = false) {
        if (newLineBefore) {
            writeln()
        }

        if (onEmptyLine) {
            writer.write(text.trimIndent().prependIndent("    ".repeat(level)))
        } else {
            writer.write(text)
        }
        onEmptyLine = false

        if (newLineAfter) {
            writeln()
        }
    }

    fun writeln(text: String) = write(text, newLineAfter = true)

    fun writeln() {
        writer.newLine()
        onEmptyLine = true
    }

    fun indent(newLineBefore: Boolean = false, newLineAfter: Boolean = false, block: CodeWriter.() -> Unit) {
        if (newLineBefore) {
            writeln()
        }
        block(CodeWriter(level + 1, writer))
        if (newLineAfter) {
            writeln()
        }
    }

}