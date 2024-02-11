package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Kotlin
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.companionMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.wrap
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class DeserializationStatementEmitter(
    private val typeDefinition: TypeDefinition,
    baseStatement: KotlinExpression,
    private val contentType: ContentType
) : CodeEmitter {

    var resultStatement = baseStatement

    override fun EmitterContext.emit() {
        resultStatement = when (typeDefinition) {
            is PrimitiveTypeDefinition -> emitForPrimitiveType(typeDefinition, resultStatement)
            is EnumTypeDefinition -> emitForEnumType(typeDefinition, resultStatement)
            is CollectionTypeDefinition -> emitForCollectionType(typeDefinition, resultStatement)
            is ObjectTypeDefinition -> emitForObjectType(typeDefinition, resultStatement)
        }

        if (!typeDefinition.nullable) {
            resultStatement = resultStatement.wrap().invoke("required".rawMethodName())
        }
    }

    private fun EmitterContext.emitForPrimitiveType(
        typeDefinition: PrimitiveTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        var result: KotlinExpression = baseStatement.invoke("as${typeDefinition.baseType.value}".methodName())
        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
        result = runEmitter(DefaultValueStatementEmitter(typeDefinition.defaultValue, result)).resultStatement
        return result
    }

    private fun EmitterContext.emitForEnumType(
        typeDefinition: EnumTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        val methodName = typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}")
        var result: KotlinExpression = baseStatement.invoke(methodName)
        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
        result = runEmitter(DefaultValueStatementEmitter(typeDefinition.defaultValue, result)).resultStatement
        return result
    }

    private fun EmitterContext.emitForCollectionType(
        typeDefinition: CollectionTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        var result: KotlinExpression = if (contentType == ContentType.ApplicationJson) {
            baseStatement.invoke("asList".rawMethodName())
        } else {
            baseStatement
        }

        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
        result = result.wrap().invoke("mapItems".methodName())
        {
            runEmitter(
                DeserializationStatementEmitter(
                    typeDefinition.items.typeDefinition,
                    "it".variableName(),
                    contentType
                )
            )
                .resultStatement.statement()
        }
        return result
    }

    private fun EmitterContext.emitForObjectType(
        typeDefinition: ObjectTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        val methodName = typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}")
        var result: KotlinExpression = baseStatement.invoke("asObject".rawMethodName())
            .wrap().invoke(methodName)
        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
        return result
    }

    companion object {

        // returns the "unsafe" type for a type in a request parameter or body
        fun TypeDefinition.getDeserializationSourceType(): TypeName {
            return when (this) {
                is CollectionTypeDefinition -> Kotlin.ListClass.typeName(true)
                    .of(items.typeDefinition.getDeserializationSourceType())

                else -> Kotlin.StringClass.typeName(true)
            }
        }

    }

}