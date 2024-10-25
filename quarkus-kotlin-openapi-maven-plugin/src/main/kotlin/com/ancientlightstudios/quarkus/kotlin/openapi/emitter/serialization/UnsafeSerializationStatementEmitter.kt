package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class UnsafeSerializationStatementEmitter(
    private val typeUsage: TypeUsage,
    baseStatement: KotlinExpression,
    private val contentType: ContentType,
    private val forceSkipNullCheck: Boolean = false
) : CodeEmitter {

    var resultStatement = baseStatement

    // if the type is null, or forced to be null, adds a null check to the statement
    //
    // e.g. if the base statement is just the variable name 'foo' it will produce 'foo?'
    override fun EmitterContext.emit() {
        if (!forceSkipNullCheck) {
            resultStatement = resultStatement.nullCheck()
        }
        resultStatement = when (val safeType = typeUsage.type) {
            is PrimitiveTypeDefinition -> emitForPrimitiveType(resultStatement)
            is EnumTypeDefinition -> emitForEnumType(resultStatement)
            is CollectionTypeDefinition -> emitForCollectionType(safeType, resultStatement)
            is ObjectTypeDefinition -> {
                if (safeType.isPureMap) {
                    emitForMapType(safeType, resultStatement)
                } else {
                    emitForObjectType(resultStatement)
                }
            }
            is OneOfTypeDefinition -> emitForOneOfType(resultStatement)
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
            ContentType.ApplicationOctetStream -> baseStatement
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
            var innerStatement = runEmitter(UnsafeSerializationStatementEmitter(typeDefinition.items, "it".variableName(), contentType))
                .resultStatement

            if (contentType == ContentType.ApplicationJson) {
                innerStatement = innerStatement.nullFallback(Misc.NullNodeClass.companionObject().property("instance".variableName()))
            }

            innerStatement.statement()
        }
    }

    private fun EmitterContext.emitForMapType(
        typeDefinition: ObjectTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        val methodName = when (contentType) {
            ContentType.ApplicationJson -> "asJson".rawMethodName()
            else -> ProbableBug("Unsupported content type $contentType for map serialization")
        }

        // produces:
        //
        // <baseStatement>.<methodName> {
        //     <SerializationStatement for nested type>
        // }
        return baseStatement.invoke(methodName) {
            var innerStatement = runEmitter(UnsafeSerializationStatementEmitter(typeDefinition.additionalProperties!!, "it".variableName(), contentType))
                .resultStatement
                innerStatement = innerStatement.nullFallback(Misc.NullNodeClass.companionObject().property("instance".variableName()))

            innerStatement.statement()
        }
    }

    // if it's an object type, generates an expression like this
    //
    // <baseStatement>.value
    private fun emitForObjectType(baseStatement: KotlinExpression): KotlinExpression {
        return when (contentType) {
            ContentType.ApplicationJson -> baseStatement.property("value".variableName())
            else -> ProbableBug("Unsupported content type $contentType for object serialization")
        }
    }

    // if it's an oneOf type, generates an expression like this
    //
    // <baseStatement>.value
    private fun emitForOneOfType(baseStatement: KotlinExpression): KotlinExpression {
        return when (contentType) {
            ContentType.ApplicationJson -> baseStatement.property("value".variableName())
            else -> ProbableBug("Unsupported content type $contentType for object serialization")
        }
    }

}
