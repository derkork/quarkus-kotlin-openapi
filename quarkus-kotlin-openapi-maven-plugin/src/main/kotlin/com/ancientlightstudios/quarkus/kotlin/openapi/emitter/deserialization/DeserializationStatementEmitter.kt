package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.companionMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.wrap
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class DeserializationStatementEmitter(
    private val typeDefinition: TypeDefinition,
    private val forceNullable: Boolean,
    baseStatement: KotlinExpression,
    private val contentType: ContentType,
    private val fromRaw: Boolean
) : CodeEmitter {

    var resultStatement = baseStatement

    // if the type is not nullable and not forced to be nullable, appends this to the generated expression
    //
    // <resultStatement>
    //    .required()
    override fun EmitterContext.emit() {
        resultStatement = when (typeDefinition) {
            is PrimitiveTypeDefinition -> emitForPrimitiveType(typeDefinition, resultStatement)
            is EnumTypeDefinition -> emitForEnumType(typeDefinition, resultStatement)
            is CollectionTypeDefinition -> emitForCollectionType(typeDefinition, resultStatement)
            is ObjectTypeDefinition -> emitForObjectType(typeDefinition, resultStatement)
        }

        if (!forceNullable && !typeDefinition.nullable) {
            resultStatement = resultStatement.wrap().invoke("required".rawMethodName())
        }
    }

    // if it's a primitive type, generates an expression like this
    //
    // <baseStatement>.as<BaseType>()
    //     [ValidationStatements]
    //     [DefaultValue]
    private fun EmitterContext.emitForPrimitiveType(
        typeDefinition: PrimitiveTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        var result = baseStatement.invoke("as${typeDefinition.baseType.value}".methodName())
        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
        result = runEmitter(DefaultValueStatementEmitter(typeDefinition.defaultValue, result)).resultStatement
        return result
    }

    // if it's an enum type, generates an expression like this
    //
    // <baseStatement>.as<ModelName>()
    //     [ValidationStatements]
    //     [DefaultValue]
    private fun EmitterContext.emitForEnumType(
        typeDefinition: EnumTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        val methodName = typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}")
        var result = baseStatement.invoke(methodName)
        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
        result = runEmitter(DefaultValueStatementEmitter(typeDefinition.defaultValue, result)).resultStatement
        return result
    }

    // if it's a collection type, generates an expression like this
    //
    // <baseStatement>[.asList()]
    //     [ValidationStatements]
    //     .mapItems {
    //         <DeserializationStatement for nested type>
    //     }
    //
    // the .asList() is only added for application/json
    private fun EmitterContext.emitForCollectionType(
        typeDefinition: CollectionTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        var result = if (contentType == ContentType.ApplicationJson) {
            baseStatement.invoke("asList".rawMethodName())
        } else {
            baseStatement
        }

        result = result.wrap().invoke("mapItems".methodName())
        {
            runEmitter(
                DeserializationStatementEmitter(
                    typeDefinition.items.typeDefinition, false, "it".variableName(), contentType, false
                )
            )
                .resultStatement.statement()
        }
        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
        return result
    }

    // if it's an object type, generates an expression like this
    //
    // if fromRaw is set to true
    //
    // <baseStatement>.asJson(objectMapper)
    //     .asObject()
    //     .as<ModelName>()
    //     [ValidationStatements]
    //
    // if fromRaw is set to false
    //
    // <baseStatement>.asObject()
    //     .as<ModelName>()
    //     [ValidationStatements]
    private fun EmitterContext.emitForObjectType(
        typeDefinition: ObjectTypeDefinition, baseStatement: KotlinExpression
    ): KotlinExpression {
        val methodName = typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}")

        var result = if (fromRaw) {
            baseStatement.invoke("asJson".rawMethodName(), "objectMapper".variableName()).wrap()
        } else {
            baseStatement
        }
        result = result.invoke("asObject".rawMethodName())
            .wrap().invoke(methodName)
        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
        return result
    }

}