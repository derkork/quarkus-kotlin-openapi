package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinAnnotation(private val name: ClassName, private vararg val parameters: Pair<VariableName, Any>) {

    fun render(writer: CodeWriter) = with(writer) {
        write("@${name.name}")

        if (parameters.isNotEmpty()) {
            write("(")
            parameters.forEachWithStats { status, (name, value) ->
                write("${name.name} = ")
                if (value is String) {
                    write("\"")
                    write(
                        value
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                    )
                    write("\"")
                } else {
                    write(value.toString())
                }

                if (!status.last) {
                    write(", ")
                }
            }
            write(")")
        }
    }
}
