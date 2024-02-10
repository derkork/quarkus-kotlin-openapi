package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.wrap
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

fun emitPlainDeserializationStatement(
    maybeStatement: KotlinExpression,
    schema: TransformableSchemaUsage
): KotlinExpression {
    val typeDefinition = schema.typeDefinition

    var result = maybeStatement
    result = when (typeDefinition) {
        is PrimitiveTypeDefinition -> emitPrimitivePlainDeserializationStatement(result, typeDefinition)
        is EnumTypeDefinition -> emitEnumPlainDeserializationStatement(result, typeDefinition)
        is CollectionTypeDefinition -> emitCollectionPlainDeserializationStatement(result, typeDefinition)
        else -> ProbableBug("plain deserialization for type ${typeDefinition.javaClass} not supported")
    }

    if (!typeDefinition.nullable) {
        result = result.wrap().invoke("required".rawMethodName())
    }
    return result
}

private fun emitPrimitivePlainDeserializationStatement(
    baseStatement: KotlinExpression,
    typeDefinition: PrimitiveTypeDefinition
): KotlinExpression {
    var result: KotlinExpression = baseStatement.invoke("as${typeDefinition.baseType.value}".methodName())
    result = emitValidationStatement(result)
    // TODO: default
    return result
}

private fun emitEnumPlainDeserializationStatement(
    baseStatement: KotlinExpression,
    typeDefinition: EnumTypeDefinition
): KotlinExpression {
    var result: KotlinExpression = baseStatement.invoke("as${typeDefinition.modelName.value}".methodName())
    result = emitValidationStatement(result)
    // TODO: default
    return result
}

private fun emitCollectionPlainDeserializationStatement(
    baseStatement: KotlinExpression,
    typeDefinition: CollectionTypeDefinition
): KotlinExpression {
    var result: KotlinExpression = baseStatement
    result = emitValidationStatement(result)
    result = result.wrap().invoke("mapItems".methodName()) {
        emitDeserializationStatement("it".variableName(), typeDefinition.items, ContentType.TextPlain).statement()

    }
    return result
}