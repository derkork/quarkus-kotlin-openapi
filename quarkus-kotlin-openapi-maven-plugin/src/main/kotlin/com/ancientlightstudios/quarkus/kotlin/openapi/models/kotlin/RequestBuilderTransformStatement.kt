package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class RequestBuilderTransformStatement(private val methodName: MethodName,
                                       private val requestContainer: ClassName?) : KotlinStatement {

    private val parameters = mutableListOf<Pair<VariableName, VariableName>>()

    fun registerParameter(parameterName: String, maybeName: VariableName) {
        parameters.add("valid $parameterName".variableName() to maybeName)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        if (requestContainer != null) {
            write("val request = maybeOf(\"request\"")
            parameters.forEach { (_, maybeName) ->
                write(", ${maybeName.name}")
            }
            write(") { (")
            parameters.forEachWithStats { stats, (validName, _) ->
                write(validName.name)
                if (!stats.last) {
                    write(", ")
                }
            }
            write(") ->")
            indent(true, true) {
                write("${requestContainer.name}(")
                indent(true, true) {
                    parameters.forEachWithStats { status, (validName, _) ->
                        write("${validName.name} as String")
                        if (!status.last) {
                            writeln(",")
                        }
                    }
                }
                write(")")
            }
            writeln("}")
            writeln("return delegate.${methodName.name}(request)")
        } else {
            writeln("return delegate.${methodName.name}()")
        }
    }

}
