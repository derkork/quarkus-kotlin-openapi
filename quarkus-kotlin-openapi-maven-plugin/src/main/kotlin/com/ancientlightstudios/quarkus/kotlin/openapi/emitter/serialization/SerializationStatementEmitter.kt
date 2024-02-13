package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class SerializationStatementEmitter(
    private val typeDefinition: TypeDefinition,
    private val forceNullable: Boolean,
    baseStatement: KotlinExpression,
    private val contentType: ContentType
) : CodeEmitter {

    var resultStatement = baseStatement

    // if the type is null, or forced to be null, adds a null check to the statement
    //
    // e.g. if the base statement is just the variable name 'foo' it will produce 'foo?'
    override fun EmitterContext.emit() {
        if (forceNullable || typeDefinition.nullable) {
            resultStatement = resultStatement.nullCheck()
        }

        resultStatement = when (typeDefinition) {
            is PrimitiveTypeDefinition -> emitForPrimitiveType(resultStatement)
            is EnumTypeDefinition -> emitForEnumType(resultStatement)
            is CollectionTypeDefinition -> emitForCollectionType(typeDefinition, resultStatement)
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

    // if it's a collection type, generates an expression like this
    //
    // <baseStatement>.asJson {
    //     <SerializationStatement for nested type>
    // }
    private fun EmitterContext.emitForCollectionType(
        typeDefinition: CollectionTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        return when (contentType) {
            ContentType.ApplicationJson -> baseStatement.invoke("asJson".rawMethodName()) {
                runEmitter(
                    SerializationStatementEmitter(
                        typeDefinition.items.typeDefinition, false, "it".variableName(), contentType
                    )
                ).resultStatement.statement()
            }

            else -> ProbableBug("Unsupported content type $contentType for collection serialization")
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
