package com.ancientlightstudios.quarkus.kotlin.openapi.utils

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

fun <T> CodeWriter.renderWithWrap(
    parameters: List<T>,
    maxSizeForSingleLine: Int = 1,
    block: CodeWriter.(T) -> Unit
) {
    // block to render the parameters. but will be called later
    val parameterBlock: CodeWriter.(Boolean, String) -> Unit = { newLine, delimiter ->
        parameters.forEachWithStats { status, it ->
            block(it)
            if (!status.last) {
                write(delimiter, newLineAfter = newLine)
            }
        }
    }

    if (parameters.size > maxSizeForSingleLine) {
        indent(newLineBefore = true, newLineAfter = true) { parameterBlock(true, ",") }
    } else {
        parameterBlock(false, ", ")
    }
}