package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import java.io.BufferedWriter

class CodeWriter private constructor(
    private val level: Int,
    private val writer: BufferedWriter,
    private var onEmptyLine: Boolean = true
) {

    constructor(writer: BufferedWriter) : this(0, writer)

    fun write(
        text: String,
        newLineBefore: Boolean = false,
        newLineAfter: Boolean = false,
        forceNewLine: Boolean = true
    ) {
        if (newLineBefore) {
            writeln(forceNewLine)
        }

        if (onEmptyLine) {
            writer.write(text.trimIndent().prependIndent("    ".repeat(level)))
        } else {
            writer.write(text)
        }
        onEmptyLine = false

        if (newLineAfter) {
            writeln(forceNewLine)
        }
    }

    fun writeln(text: String, forceNewLine: Boolean = true) =
        write(text, newLineAfter = true, forceNewLine = forceNewLine)

    fun writeln(forceNewLine: Boolean = true) {
        if (onEmptyLine && !forceNewLine) {
            return
        }

        writer.newLine()
        onEmptyLine = true
    }

    fun indent(
        newLineBefore: Boolean = false,
        newLineAfter: Boolean = false,
        forceNewLine: Boolean = false,
        block: CodeWriter.() -> Unit
    ) {
        if (newLineBefore) {
            writeln(forceNewLine)
        }

        // tell the nested writer if there is already content on the current line
        CodeWriter(level + 1, writer, onEmptyLine)
            .apply(block)
            // keep the information from the nested writer
            .also { onEmptyLine = it.onEmptyLine }
        if (newLineAfter) {
            writeln(forceNewLine)
        }
    }

}