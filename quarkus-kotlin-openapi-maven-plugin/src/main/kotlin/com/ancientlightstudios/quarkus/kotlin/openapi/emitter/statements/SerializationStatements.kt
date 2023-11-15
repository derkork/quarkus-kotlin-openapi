package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.CollectionTypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.EnumTypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.InlinePrimitiveTypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.TypeDefinitionUsage

fun CodeWriter.writeSerializationStatement(variableName:VariableName, type:TypeDefinitionUsage) {
    write(variableName.render())
    if (type.nullable) {
        write("?")
    }

    when (type) {
        is EnumTypeUsage -> write(".from${type.name.render()}")
        is InlinePrimitiveTypeUsage -> write(".${type.serializeMethodName.render()}")
        is CollectionTypeUsage -> write(".fromList")
        else -> write(".toJsonNode")
    }

    if (type is CollectionTypeUsage) {
        write(" ")
        block {
            writeSerializationStatement("it".variableName(), type.innerType)
        }
    } else {
        write("()")
    }

}