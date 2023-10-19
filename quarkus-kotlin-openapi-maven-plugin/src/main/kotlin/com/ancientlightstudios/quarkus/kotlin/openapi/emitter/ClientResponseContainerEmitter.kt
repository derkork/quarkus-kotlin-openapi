package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendFromClassExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ConstantName.Companion.rawConstantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import jakarta.ws.rs.core.Response

class ClientResponseContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        suite.requests.forEach {
            emitResponseContainer(it)
        }
    }

    private fun EmitterContext.emitResponseContainer(request: Request) {
        kotlinFile(clientPackage(), request.name.extend(postfix = "Response").className()) {
            registerImport(modelPackage(), wildcardImport = true)
            registerImport("jakarta.ws.rs.core.Response")

            kotlinClass(fileName, sealed = true) {
                kotlinMember("status".variableName(), "Response.Status".rawTypeName(), private = false, open = true)
                kotlinMember("unsafeBody".variableName(), "Any".rawTypeName(true), private = false, open = true)

                request.responses.forEach { (responseCode, typeDefinitionUsage) ->
                    when (responseCode) {
                        is ResponseCode.Default -> emitDefaultResponseClass(
                            fileName,
                            typeDefinitionUsage?.safeType
                        )

                        is ResponseCode.HttpStatusCode -> emitResponseClass(
                            fileName,
                            responseCode,
                            typeDefinitionUsage?.safeType
                        )
                    }
                }
            }
        }.also { generateFile(it) }
    }

    private fun ClassAware.emitDefaultResponseClass(parentClass: ClassName, bodyType: TypeName?) {
        val statusCodeExpression = "status".variableName().pathExpression()
        val bodyExpression = when(bodyType) {
            null -> NullExpression
            else -> "safeBody".variableName().pathExpression()
        }
        val extends = listOf(ExtendFromClassExpression(parentClass, statusCodeExpression, bodyExpression))

        kotlinClass("Default".className(), asDataClass = true, extends = extends) {
            kotlinMember("status".variableName(), "Response.Status".rawTypeName(), private = false, override = true)
            bodyType?.let {
                kotlinMember("safeBody".variableName(), bodyType, private = false)
            }
        }
    }

    private fun ClassAware.emitResponseClass(
        parentClass: ClassName,
        responseCode: ResponseCode.HttpStatusCode,
        bodyType: TypeName?
    ) {
        val statusName = Response.Status.fromStatusCode(responseCode.value).name
        val statusCodeExpression = "Response.Status".rawClassName().pathExpression().then(statusName.rawConstantName())
        val bodyExpression = when(bodyType) {
            null -> NullExpression
            else -> "safeBody".variableName().pathExpression()
        }
        val extends = listOf(ExtendFromClassExpression(parentClass, statusCodeExpression, bodyExpression))

        kotlinClass(responseCode.statusCodeReason().className(), asDataClass = bodyType != null, extends = extends) {
            bodyType?.let {
                kotlinMember("safeBody".variableName(), bodyType, private = false)
            }
        }
    }

    private fun ResponseCode.HttpStatusCode.statusCodeReason() =
        Response.Status.fromStatusCode(value)?.reasonPhrase ?: "status${value}"

}
