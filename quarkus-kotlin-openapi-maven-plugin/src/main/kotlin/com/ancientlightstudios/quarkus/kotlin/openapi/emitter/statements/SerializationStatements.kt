package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.CollectionTypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.TypeDefinitionUsage

fun CodeWriter.writeToJsonNode(variableName:VariableName, type:TypeDefinitionUsage) {
    write(variableName.render())
    if (type.nullable) {
        write("?")
    }
    write(".toJsonNode")

    if (type is CollectionTypeUsage) {
        write(" ")
        block {
            writeToJsonNode("it".variableName(), type.innerType)
        }
    } else {
        write("()")
    }

}