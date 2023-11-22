package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.CollectionTypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.EnumTypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.PrimitiveTypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.TypeDefinitionUsage

fun CodeWriter.writeSerializationStatement(variableName:Expression, type:TypeDefinitionUsage) {
    write(variableName.evaluate())
    if (type.nullable) {
        write("?")
    }

    when (type) {
        is EnumTypeUsage -> write(".from${type.name.render()}")
        is PrimitiveTypeUsage -> write(".${type.serializeMethodName.render()}")
        is CollectionTypeUsage -> write(".fromList")
        else -> write(".toJsonNode")
    }


    if (type is CollectionTypeUsage) {
        write(" ")
        block {
            writeSerializationStatement("it".variableName().pathExpression(), type.innerType)
        }
    } else {
        write("()")
    }

}