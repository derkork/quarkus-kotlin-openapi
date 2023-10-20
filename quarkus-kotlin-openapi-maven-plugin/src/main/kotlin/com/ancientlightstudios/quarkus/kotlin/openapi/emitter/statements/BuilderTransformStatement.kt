package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

abstract class BuilderTransformStatement : KotlinStatement {

    private val parameters = mutableListOf<VariableName>()

    fun addParameter(maybeName: VariableName) {
        parameters.add(maybeName)
    }

    protected fun CodeWriter.renderTransformStatement(objectName: ClassName) {
        write("maybeOf(\"request\"")
        parameters.forEach { maybeName ->
            write(", ${maybeName.render()}")
        }
        write(") {")
        indent(newLineBefore = true, newLineAfter = true) {
            write("${objectName.render()}(")
            indent(newLineBefore = true, newLineAfter = true) {
                parameters.forEachWithStats { status, maybeName ->
                    write("(${maybeName.render()} as Maybe.Success).value")
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
            writeln("return delegate.${methodName.render()}(request).response")
        } else {
            writeln("return delegate.${methodName.render()}().response")
        }
    }

}