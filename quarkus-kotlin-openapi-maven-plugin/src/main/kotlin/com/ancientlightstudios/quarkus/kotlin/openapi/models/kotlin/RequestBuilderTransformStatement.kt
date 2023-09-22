package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class RequestBuilderTransformStatement(
    private val methodName: MethodName,
    private val requestContainer: ClassName?
) : KotlinStatement {

    private val parameters = mutableListOf<Pair<VariableName, VariableName>>()

    fun registerParameter(parameterName: String, maybeName: VariableName) {
        parameters.add(Pair("valid $parameterName".variableName(), maybeName))
    }

    override fun render(writer: CodeWriter) = with(writer) {
        if (requestContainer != null) {
            write("val request = maybeOf(\"request\"")
            parameters.forEach { (_, maybeName) ->
                write(", ${maybeName.name}")
            }
            write(") { args ->")
            indent(newLineBefore = true, newLineAfter = true) {
                write("${requestContainer.name}(")
                indent(newLineBefore = true, newLineAfter = true) {
                    parameters.forEachWithStats { status, _ ->
                        write("maybeCast(args[${status.index}])")
                        if (!status.last) {
                            writeln(",")
                        }
                    }
                }
                write(")")
            }
            writeln("}")
            writeln("return delegate.${methodName.name}(request).response")
        } else {
            writeln("return delegate.${methodName.name}().response")
        }
    }

}
