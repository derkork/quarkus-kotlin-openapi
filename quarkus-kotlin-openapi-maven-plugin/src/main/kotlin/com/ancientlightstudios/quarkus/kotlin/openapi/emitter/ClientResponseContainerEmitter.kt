package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendFromClassExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendFromInterfaceExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ConstantName.Companion.rawConstantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
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
            registerImport("org.jboss.resteasy.reactive.RestResponse")
            registerImport("jakarta.ws.rs.core.Response")
            registerImport("com.ancientlightstudios.quarkus.kotlin.openapi.IsError")


            kotlinInterface(fileName) {}

            val httpResponseName = request.name.extend(postfix = "HttpResponse").className()

            kotlinClass(httpResponseName, sealed = true, extends = listOf(ExtendFromInterfaceExpression(fileName))) {
                kotlinMember("status".variableName(), "RestResponse.Status".rawTypeName(), accessModifier = null, open = true)
                kotlinMember("unsafeBody".variableName(), "Any".rawTypeName(true), accessModifier = null, open = true)

                request.responses.forEach { (responseCode, typeDefinitionUsage) ->
                    when (responseCode) {
                        is ResponseCode.Default -> emitDefaultResponseClass(
                            httpResponseName,
                            typeDefinitionUsage?.safeType
                        )

                        is ResponseCode.HttpStatusCode -> emitResponseClass(
                            httpResponseName,
                            responseCode,
                            typeDefinitionUsage?.safeType
                        )
                    }
                }
            }

            val errorInterfaceName = request.name.extend(postfix = "Error").className()
            kotlinInterface(errorInterfaceName, sealed = true, extends = listOf(ExtendFromInterfaceExpression(fileName), ExtendFromInterfaceExpression("IsError".rawClassName()))) {
                kotlinClass("RequestErrorTimeout".className(), extends = listOf(ExtendFromInterfaceExpression(errorInterfaceName))) {
                    kotlinMember("errorMessage".variableName(), "String".rawTypeName(), accessModifier = null, override = true, initializedInConstructor = false,
                        default = "A timeout occurred when communicating with the server.".stringExpression())
                }

                kotlinClass("RequestErrorUnreachable".className(), extends = listOf(ExtendFromInterfaceExpression(errorInterfaceName))) {
                    kotlinMember("errorMessage".variableName(), "String".rawTypeName(), accessModifier = null, override = true, initializedInConstructor = false,
                        default = "The server could not be reached.".stringExpression())
                }

                kotlinClass("RequestErrorConnectionReset".className(), extends = listOf(ExtendFromInterfaceExpression(errorInterfaceName))) {
                    kotlinMember("errorMessage".variableName(), "String".rawTypeName(), accessModifier = null, override = true, initializedInConstructor = false,
                        default = "The connection was reset while communicating with the server.".stringExpression())
                }

                kotlinClass("RequestErrorUnknown".className(), extends = listOf(ExtendFromInterfaceExpression(errorInterfaceName))) {
                    kotlinMember("errorMessage".variableName(), "String".rawTypeName(), accessModifier = null, override = true, initializedInConstructor = false,
                        default = "An unknown error occurred when communicating with the server.".stringExpression())
                }

                kotlinClass("ResponseError".className(), extends = listOf(ExtendFromInterfaceExpression(errorInterfaceName)))   {
                    kotlinMember("reason".variableName(), "String".rawTypeName(), accessModifier = null, override = false)
                    kotlinMember("response".variableName(), "Response".rawTypeName(), accessModifier = null, override = false)
                    kotlinMember("errorMessage".variableName(), "String".rawTypeName(), accessModifier = null, override = true, initializedInConstructor = false,
                        default = "reason".variableName().pathExpression())
                }
            }


        }.also { generateFile(it) }
    }

    private fun ClassAware.emitDefaultResponseClass(parentClass: ClassName, bodyType: TypeName?) {
        val statusCodeExpression = "status".variableName().pathExpression()
        val bodyExpression = when (bodyType) {
            null -> NullExpression
            else -> "safeBody".variableName().pathExpression()
        }
        val extends = listOf(ExtendFromClassExpression(parentClass, statusCodeExpression, bodyExpression))

        kotlinClass("Default".className(), asDataClass = true, extends = extends) {
            kotlinMember("status".variableName(), "RestResponse.Status".rawTypeName(), accessModifier = null, override = true)
            bodyType?.let {
                kotlinMember("safeBody".variableName(), bodyType, accessModifier = null)
            }
        }
    }

    private fun ClassAware.emitResponseClass(
        parentClass: ClassName,
        responseCode: ResponseCode.HttpStatusCode,
        bodyType: TypeName?
    ) {
        val statusName = Response.Status.fromStatusCode(responseCode.value).name
        val statusCodeExpression =
            "RestResponse.Status".rawClassName().pathExpression().then(statusName.rawConstantName())
        val bodyExpression = when (bodyType) {
            null -> NullExpression
            else -> "safeBody".variableName().pathExpression()
        }
        val extends = listOf(ExtendFromClassExpression(parentClass, statusCodeExpression, bodyExpression))

        kotlinClass(responseCode.statusCodeReason().className(), asDataClass = bodyType != null, extends = extends) {
            bodyType?.let {
                kotlinMember("safeBody".variableName(), bodyType, accessModifier = null)
            }
        }
    }

    private fun ResponseCode.HttpStatusCode.statusCodeReason() =
        Response.Status.fromStatusCode(value)?.reasonPhrase ?: "status${value}"

}
