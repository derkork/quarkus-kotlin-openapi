package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
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