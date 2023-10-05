package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinEnum(name: ClassName, private val items: List<Pair<String, ClassName>>, private val defaultValue: String?) : KotlinFileContent(name) {

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)

        write("enum class ${name.name}(val value: String) {")
        indent(newLineBefore = true, newLineAfter = true) {
            items.forEachWithStats { status, (value, name) ->
                writeln("@JsonProperty(\"$value\")")
                write("${name.name}(\"$value\")")
                if (!status.last) {
                    writeln(",")
                }
            }
        }
        writeln("}")

        writeln()
        writeln("fun String?.as${name.name}(context:String) : Maybe<${name.name}?> {")
        indent {
            writeln("if (this == null) {")
            indent {
                if (defaultValue == null) {
                    writeln("return asMaybe(context)")
                } else {
                    val enumItem = items.first { it.first == defaultValue }.second
                    writeln("return ${name.name}.${enumItem.name}.asMaybe(context)")
                }
            }
            writeln("}")
            writeln("return when (this) {")
            indent {
                items.forEach { (value, itemName) ->
                    writeln("\"$value\" -> ${name.name}.${itemName.name}.asMaybe(context)")
                }
                writeln("else -> Maybe.Failure(context, ValidationError(\"Invalid value for ${name.name}: \$this\"))")
            }
            writeln("}")
        }
        writeln("}")

        writeln()
        // this function should only be used for list/array items. otherwise the default value will not be available
        writeln("fun Maybe<String?>.as${name.name}() : Maybe<${name.name}?> = onNotNull { value.as${name.name}(context) }")
    }
}

