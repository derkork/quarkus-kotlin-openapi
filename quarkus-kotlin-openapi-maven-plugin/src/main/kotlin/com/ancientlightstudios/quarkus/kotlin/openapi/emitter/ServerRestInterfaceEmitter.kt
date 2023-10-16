package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.VariableName.Companion.variableName

class ServerRestInterfaceEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite) {
        val fileName = suite.name.extend(postfix = "Server")
        kotlinFile(serverPackage(), fileName) {
            registerImport("jakarta.ws.rs.Path")
            registerImport("com.fasterxml.jackson.databind.ObjectMapper")

            kotlinClass(fileName, false) {
                addPathAnnotation("/")

                kotlinMember("delegate".variableName(), suite.name.extend(postfix = "Delegate").typeName())
                kotlinMember("objectMapper".variableName(), "ObjectMapper".rawTypeName())
            }
        }.also { generateFile(it) }
    }
}

// enum
/*
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
 */

// value class
/*
        writeln("fun String?.as${name.name}(context:String) : Maybe<${name.name}?> {")
        indent {
            if( defaultValue != null) {
                // TODO: we should replace this with a 'map { it ?: defaultValue }' to avoid parsing the value each time. but this class needs to know jow to convert the value into the required data type
                write("return (this ?: \"$defaultValue\").as")
            } else {
                write("return as")
            }
            nestedType.render(this)
            writeln("(context)")
            indent {
                // TODO: add type validation here if necessary e.g. validateString { it.minLength(5) }
                writeln(".mapNotNull { ${name.name}(it) }")
            }
        }
        writeln("}")

        writeln()
        // this function should only be used for list/array items. otherwise the default value will not be available
        writeln("fun Maybe<String?>.as${name.name}() : Maybe<${name.name}?> = onNotNull { value.as${name.name}(context) }")

 */