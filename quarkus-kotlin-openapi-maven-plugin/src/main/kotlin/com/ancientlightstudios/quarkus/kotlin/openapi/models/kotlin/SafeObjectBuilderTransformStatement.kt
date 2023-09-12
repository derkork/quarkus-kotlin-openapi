package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class SafeObjectBuilderTransformStatement(
    private val safeObject: ClassName
) : KotlinStatement {

    private val parameters = mutableListOf<Pair<VariableName, VariableName>>()

    fun addParameter(parameterName: String, maybeName: VariableName) {
        parameters.add(Pair("valid $parameterName".variableName(), maybeName))
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("return maybeOf(\"request\"")
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
        indent(newLineBefore = true, newLineAfter = true) {
            write("${safeObject.name}(")
            indent(newLineBefore = true, newLineAfter = true) {
                parameters.forEachWithStats { status, (validName, _) ->
                    write("maybeCast(${validName.name})")
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
