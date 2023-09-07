package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinEnum(name: ClassName, private val items: List<Pair<String, ClassName>>) : KotlinFileContent(name) {

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)

        write("enum class ${name.name}(val value: String) {")
        indent(newLineBefore = true, newLineAfter = true) {
            items.forEachWithStats { status, (value, name) ->
                writeln("@JsonProperty(\"$value\")")
                write("${name.name}(\"$value\")")
                if (!status.last) {
                    writeln(",")
                    writeln()
                }
            }
        }
        write("}")
    }
}
