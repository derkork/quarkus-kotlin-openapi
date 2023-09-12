package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinEnum(name: ClassName, private val items: List<Pair<String, ClassName>>) : KotlinFileContent(name) {


    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)

        write("enum class ${name.name}(val value: String) {")
        indent(newLineBefore = true, newLineAfter = true) {
            items.forEachWithStats { status, (value, name) ->
                write("${name.name}(\"$value\")")
                if (!status.last) {
                    writeln(",")
                }
            }

            writeln()
        }
        writeln("}")

        writeln()
        writeln("fun String?.as${name.name}(context:String) : Maybe<${name.name}?> {")
        indent {
            writeln("if (this == null) {")
            indent {
                writeln("maybeOf(context)")
            }
            writeln("}")
            writeln("return when (this) {")
            indent {
                items.forEach { (value, itemName) ->
                    writeln("\"$value\" -> ${name.name}.${itemName.name}.maybeOf(context)")
                }
                writeln("else -> failedMaybeOf(context,  \"Invalid value for ${name.name}: \$this\")")
            }
            writeln("}")
        }
        writeln("}")
    }
}

