package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class AnyOfBuilderTransformStatement(val objectName: ClassName) : KotlinStatement {

    private val parameters = mutableListOf<VariableName>()

    fun addParameter(maybeName: VariableName) {
        parameters.add(maybeName)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("return maybeAnyOf(context")
        parameters.forEach { maybeName ->
            write(", ${maybeName.render()}")
        }
        write(") {")
        indent(newLineBefore = true, newLineAfter = true) {
            write("${objectName.render()}(")
            indent(newLineBefore = true, newLineAfter = true) {
                parameters.forEachWithStats { status, maybeName ->
                    write("${maybeName.render()}.validValueOrNull()")
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
