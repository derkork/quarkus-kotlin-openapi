package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.strafbank.CodeWriter

class KotlinAnnotation(private val name: Name.ClassName, private vararg val parameters: Pair<Name.VariableName, Any>) {
    fun render(writer: CodeWriter) {
        writer.write("@${name.name}")

        if (parameters.isNotEmpty()) {
            writer.write("(")
            parameters.forEachIndexed { index, (name, value) ->
                if (index > 0) {
                    writer.write(", ")
                }

                writer.write("${name.name} = ")
                if (value is String) {
                    writer.write("\"")
                    writer.write(
                        value
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\""))
                    writer.write("\"")
                }
                else {
                    writer.write(value.toString())
                }
            }
            writer.write(")")
        }
    }
}
