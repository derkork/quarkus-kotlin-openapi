package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class SerializationStatementEmitter(
    private val typeUsage: TypeUsage,
    baseStatement: KotlinExpression,
    private val contentType: ContentType
) : CodeEmitter {

    var resultStatement = baseStatement

    // if the type is null, or forced to be null, adds a null check to the statement
    //
    // e.g. if the base statement is just the variable name 'foo' it will produce 'foo?'
    override fun EmitterContext.emit() {
        if (typeUsage.nullable) {
            resultStatement = resultStatement.nullCheck()
        }

        resultStatement = when (val safeType = typeUsage.type) {
            is PrimitiveTypeDefinition -> emitForPrimitiveType(resultStatement)
            is EnumTypeDefinition -> emitForEnumType(resultStatement)
            is CollectionTypeDefinition -> emitForCollectionType(safeType, resultStatement)
            is ObjectTypeDefinition -> emitForObjectType(resultStatement)
        }
    }

    // if it's a primitive type, generates an expression like this
    //
    // for plain/text
    //
    // <baseStatement>.asString()
    //
    // for application/json
    //
    // <baseStatement>.asJson()
    private fun emitForPrimitiveType(baseStatement: KotlinExpression): KotlinExpression {
        return when (contentType) {
            ContentType.TextPlain -> baseStatement.invoke("asString".rawMethodName())
            ContentType.ApplicationJson -> baseStatement.invoke("asJson".rawMethodName())
            else -> ProbableBug("Unsupported content type $contentType for primitive serialization")
        }
    }

    // if it's an enum type, generates an expression like this
    //
    // for plain/text
    //
    // <baseStatement>.asString()
    //
    // for application/json
    //
    // <baseStatement>.asJson()
    private fun emitForEnumType(baseStatement: KotlinExpression): KotlinExpression {
        return when (contentType) {
            ContentType.TextPlain -> baseStatement.invoke("asString".rawMethodName())
            ContentType.ApplicationJson -> baseStatement.invoke("asJson".rawMethodName())
            else -> ProbableBug("Unsupported content type $contentType for enum serialization")
        }
    }

    private fun EmitterContext.emitForCollectionType(
        typeDefinition: CollectionTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        val methodName = when (contentType) {
            ContentType.ApplicationJson -> "asJson".rawMethodName()
            ContentType.TextPlain -> "map".rawMethodName()
            else -> ProbableBug("Unsupported content type $contentType for collection serialization")
        }

        // produces:
        //
        // <baseStatement>.<methodName> {
        //     <SerializationStatement for nested type>
        // }
        return baseStatement.invoke(methodName) {
            runEmitter(SerializationStatementEmitter(typeDefinition.items, "it".variableName(), contentType))
                .resultStatement.statement()
        }
    }

    // if it's an object type, generates an expression like this
    //
    // <baseStatement>.asJson()
    private fun emitForObjectType(baseStatement: KotlinExpression): KotlinExpression {
        return when (contentType) {
            ContentType.ApplicationJson -> baseStatement.invoke("asJson".rawMethodName())
            else -> ProbableBug("Unsupported content type $contentType for object serialization")
        }
    }

}
