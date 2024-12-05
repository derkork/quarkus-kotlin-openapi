package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.isNullable
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.companionMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.wrap
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

//class DeserializationStatementEmitter(
//    private val typeUsage: TypeUsage,
//    baseStatement: KotlinExpression,
//    private val contentType: ContentType,
//    private val fromRaw: Boolean  // true if it is teh value of a parameter or body, false if it is from within an object
//) : CodeEmitter {
//
//    var resultStatement = baseStatement
//
//    // if the type is not nullable and not forced to be nullable, appends this to the generated expression
//    //
//    // <resultStatement>
//    //    .required()
//    override fun EmitterContext.emit() {
//        if (contentType == ContentType.ApplicationJson && fromRaw) {
//            resultStatement = resultStatement.invoke("asJson".rawMethodName(), "objectMapper".variableName()).wrap()
//        }
//
//        resultStatement = when (val safeType = typeUsage.type) {
//            is PrimitiveTypeDefinition -> emitForPrimitiveType(safeType, resultStatement)
//            is EnumTypeDefinition -> emitForEnumType(safeType, resultStatement)
//            is CollectionTypeDefinition -> emitForCollectionType(safeType, resultStatement)
//            is ObjectTypeDefinition -> {
//                if (safeType.isPureMap) {
//                    emitForMapType(safeType, resultStatement)
//                } else {
//                    emitForObjectType(safeType, resultStatement)
//                }
//            }
//            is OneOfTypeDefinition -> emitForOneOfType(safeType, resultStatement)
//        }
//
//        if (!typeUsage.isNullable()) {
//            resultStatement = resultStatement.wrap().invoke("required".rawMethodName())
//        }
//    }
//
//    // if it's a primitive type, generates an expression like this
//    //
//    // <baseStatement>.as<BaseType>()
//    //     [ValidationStatements]
//    //     [DefaultValue]
//    private fun EmitterContext.emitForPrimitiveType(
//        typeDefinition: PrimitiveTypeDefinition, baseStatement: KotlinExpression
//    ): KotlinExpression {
//        // TODO: we probably need something like this for other contenttype and type definition combinations too
//        var result = if (contentType == ContentType.ApplicationOctetStream) {
//            baseStatement.invoke("emptyArrayAsNull".methodName()).wrap()
//        } else {
//            baseStatement.invoke("as${typeDefinition.baseType.value}".rawMethodName())
//        }
//
//        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
//        result = runEmitter(DefaultValueStatementEmitter(typeDefinition.defaultExpression(), result)).resultStatement
//        return result
//    }
//
//    // if it's an enum type, generates an expression like this
//    //
//    // <baseStatement>.as<ModelName>()
//    //     [ValidationStatements]
//    //     [DefaultValue]
//    private fun EmitterContext.emitForEnumType(
//        typeDefinition: EnumTypeDefinition, baseStatement: KotlinExpression
//    ): KotlinExpression {
//        var result = if (contentType == ContentType.TextPlain) {
//            baseStatement.invoke("emptyStringAsNull".methodName()).wrap()
//        } else {
//            baseStatement
//        }
//        result = result.invoke(typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}"))
//        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
//        result = runEmitter(DefaultValueStatementEmitter(typeDefinition.defaultExpression(), result)).resultStatement
//        return result
//    }
//
//    // if it's a collection type, generates an expression like this
//    //
//    // <baseStatement>[.asList()]
//    //     .mapItems {
//    //         <DeserializationStatement for nested type>
//    //     }
//    //     [ValidationStatements]
//    //
//    // the .asList() is only added for application/json
//    private fun EmitterContext.emitForCollectionType(
//        typeDefinition: CollectionTypeDefinition, baseStatement: KotlinExpression
//    ): KotlinExpression {
//        var result = baseStatement
//
//        if (contentType == ContentType.ApplicationJson) {
//            result = result.invoke("asList".rawMethodName())
//        }
//
//        result = result.wrap().invoke("mapItems".methodName())
//        {
//            runEmitter(DeserializationStatementEmitter(typeDefinition.items, "it".variableName(), contentType, false))
//                .resultStatement.statement()
//        }
//        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
//        return result
//    }
//
//    // if it's a map type, generates an expression like this
//    //
//    // <baseStatement>[.asObject()]
//    //     .propertiesAsMap {
//    //         <DeserializationStatement for nested type>
//    //     }
//    //     [ValidationStatements]
//    //
//    // the .asObject() is only added for application/json
//    private fun EmitterContext.emitForMapType(
//        typeDefinition: ObjectTypeDefinition, baseStatement: KotlinExpression
//    ): KotlinExpression {
//        var result = baseStatement
//
//        if (contentType == ContentType.ApplicationJson) {
//            result = result.invoke("asObject".rawMethodName())
//        }
//
//        result = result.wrap().invoke("propertiesAsMap".methodName())
//        {
//            runEmitter(DeserializationStatementEmitter(typeDefinition.additionalProperties!!, "it".variableName(), contentType, false))
//                .resultStatement.statement()
//        }
//        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
//        return result
//    }
//
//    // if it's an object type, generates an expression like this
//    //
//    // if fromRaw is set to true
//    //
//    // <baseStatement>.asJson(objectMapper)
//    //     .asObject()
//    //     .as<ModelName>()
//    //     [ValidationStatements]
//    //
//    // if fromRaw is set to false
//    //
//    // <baseStatement>.asObject()
//    //     .as<ModelName>()
//    //     [ValidationStatements]
//    private fun EmitterContext.emitForObjectType(
//        typeDefinition: ObjectTypeDefinition, baseStatement: KotlinExpression
//    ): KotlinExpression {
//        val methodName = typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}")
//
//        var result = baseStatement.invoke("asObject".rawMethodName())
//            .wrap().invoke(methodName)
//        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
//        return result
//    }
//
//    // if it's an oneOf type, generates an expression like this
//    //
//    // if fromRaw is set to true
//    //
//    // <baseStatement>.asJson(objectMapper)
//    //     .asObject()
//    //     .as<ModelName>()
//    //     [ValidationStatements]
//    //
//    // if fromRaw is set to false
//    //
//    // <baseStatement>.asObject()
//    //     .as<ModelName>()
//    //     [ValidationStatements]
//    private fun EmitterContext.emitForOneOfType(
//        typeDefinition: OneOfTypeDefinition, baseStatement: KotlinExpression
//    ): KotlinExpression {
//        var result = baseStatement.invoke("asObject".rawMethodName()).wrap()
//            .invoke(typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}"))
//        result = runEmitter(ValidationStatementEmitter(typeDefinition, result)).resultStatement
//        return result
//    }
//
//}