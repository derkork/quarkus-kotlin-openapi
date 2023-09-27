package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

abstract class BuilderTransformStatement : KotlinStatement {

    private val parameters = mutableListOf<VariableName>()

    fun addParameter(maybeName: VariableName) {
        parameters.add(maybeName)
    }

    protected fun CodeWriter.renderTransformStatement(objectName: ClassName) {
        write("maybeOf(\"request\"")
        parameters.forEach { maybeName ->
            write(", ${maybeName.name}")
        }
        write(") {")
        indent(newLineBefore = true, newLineAfter = true) {
            write("${objectName.name}(")
            indent(newLineBefore = true, newLineAfter = true) {
                parameters.forEachWithStats { status, maybeName ->
                    write("(${maybeName.name} as Maybe.Success).value")
                    if (!status.last) {
                        writeln(",")
                    }
                }
            }
            write(")")
        }
        writeln("}")
    }

}

class SafeObjectBuilderTransformStatement(private val safeObject: ClassName) : BuilderTransformStatement() {

    override fun render(writer: CodeWriter) = with(writer) {
        write("return ")
        renderTransformStatement(safeObject)
    }

}

class RequestBuilderTransformStatement(
    private val methodName: MethodName,
    private val requestContainer: ClassName?
) : BuilderTransformStatement() {

    override fun render(writer: CodeWriter) = with(writer) {
        if (requestContainer != null) {
            write("val request = ")
            renderTransformStatement(requestContainer)
            writeln("return delegate.${methodName.name}(request).response")
        } else {
            writeln("return delegate.${methodName.name}().response")
        }
    }

}