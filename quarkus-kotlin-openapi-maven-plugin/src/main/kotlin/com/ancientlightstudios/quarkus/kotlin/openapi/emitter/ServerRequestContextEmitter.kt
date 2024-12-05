package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Library
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContext

class ServerRequestContextEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerRequestContext>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(requestContext: ServerRequestContext) {
        kotlinFile(requestContext.name.asTypeName()) {
            registerImports(Library.All)

            kotlinClass(name) {
            }
        }
    }

//    private fun RequestInspection.emitContainerFile() = kotlinFile(request.requestContextClassName) {
//        val defaultResponseExists = request.responses.any { it.responseCode == ResponseCode.Default }
//
//        registerImports(Library.AllClasses)
//        registerImports(emitterContext.getAdditionalImports())
//
//        val interfaces = mutableListOf(Library.RequestContextInterface)
//        // the generic status interface if allowed
//        if (!defaultResponseExists) {
//            interfaces.add(Library.ResponseWithGenericStatusInterface)
//        }
//
//        // all interfaces defined by the responses
//        interfaces.addAll(request.responses.mapNotNull { it.responseInterfaceName }.toSet())
//
//        kotlinClass(fileName, interfaces = interfaces) {
//            if (request.hasInputParameter()) {
//                val requestType =
//                    Library.MaybeClass.typeName().of(request.requestContainerClassName.typeName())
//                kotlinMember("request".variableName(), requestType, accessModifier = null)
//            }
//            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())
//            kotlinMember("headers".variableName(), Jakarta.HttpHeadersClass.typeName())
//
//            emitGenericStatusMethod(defaultResponseExists)
//
//            request.responses.forEach {
//                when (val code = it.responseCode) {
//                    is ResponseCode.HttpStatusCode -> emitStatusMethod(
//                        code,
//                        it.body,
//                        it.headers,
//                        it.responseInterfaceName
//                    )
//
//                    is ResponseCode.Default -> emitDefaultStatusMethod(it.body, it.headers, it.responseInterfaceName)
//                }
//            }
//
//            emitRawHeaderMethods()
//
//            emitInterfaceMembers(request)
//        }
//    }
//
//    private fun KotlinClass.emitGenericStatusMethod(defaultResponseExists: Boolean) {
//        val accessModifier = when (defaultResponseExists) {
//            true -> KotlinAccessModifier.Private
//            false -> null
//        }
//
//        val parameterDefaultValue = when (defaultResponseExists) {
//            true -> nullLiteral()
//            false -> null
//        }
//
//        val statusVariable = "status".variableName()
//        val typeVariable = "mediaType".variableName()
//        val bodyVariable = "body".variableName()
//        val headersVariable = "headers".variableName()
//
//        kotlinMethod(
//            "status".methodName(), bodyAsAssignment = true, accessModifier = accessModifier,
//            returnType = Kotlin.NothingType, override = !defaultResponseExists
//        ) {
//            kotlinParameter(statusVariable, Kotlin.IntClass.typeName())
//            kotlinParameter(typeVariable, Kotlin.StringClass.typeName(true), parameterDefaultValue)
//            kotlinParameter(bodyVariable, Kotlin.AnyClass.typeName(true), parameterDefaultValue)
//            kotlinParameter(
//                headersVariable, Kotlin.PairClass.typeName().of(
//                    Kotlin.StringClass.typeName(), Kotlin.AnyClass.typeName(true)
//                ), asParameterList = true
//            )
//
//            val statement = Misc.ResponseBuilderClass.companionObject()
//                .invoke("create".rawMethodName(), statusVariable, genericTypes = listOf(Kotlin.AnyClass.typeName(true)))
//                .invoke("entity".rawMethodName(), bodyVariable)
//                .wrap()
//                .invoke("type".rawMethodName(), typeVariable)
//                .invoke("apply".rawMethodName()) {
//                    headersVariable.invoke("forEach".rawMethodName()) {
//                        invoke(
//                            "headers".rawMethodName(),
//                            "it".variableName().property("first".variableName()),
//                            "it".variableName().property("second".variableName())
//                        ).statement()
//                    }.statement()
//                }
//                .invoke("build".rawMethodName())
//            invoke(Library.RequestHandledSignalClass.constructorName, statement).throwStatement()
//        }
//    }
//
//    private fun KotlinClass.emitStatusMethod(
//        statusCode: ResponseCode.HttpStatusCode,
//        body: OpenApiBody?,
//        headers: List<OpenApiParameter>,
//        responseInterfaceName: ClassName?
//    ) {
//        kotlinMethod(
//            statusCode.statusCodeReason().methodName(), bodyAsAssignment = true,
//            returnType = Kotlin.NothingType, override = responseInterfaceName != null
//        ) {
//            emitMethodBody(statusCode.value.literal(), body, headers)
//        }
//    }
//
//    private fun KotlinClass.emitDefaultStatusMethod(
//        body: OpenApiBody?,
//        headers: List<OpenApiParameter>,
//        responseInterfaceName: ClassName?
//    ) {
//        kotlinMethod(
//            "defaultStatus".methodName(), bodyAsAssignment = true,
//            returnType = Kotlin.NothingType, override = responseInterfaceName != null
//        ) {
//            val statusVariable = "status".variableName()
//            kotlinParameter(statusVariable, Kotlin.IntClass.typeName())
//            emitMethodBody(statusVariable, body, headers)
//        }
//    }
//
//    private fun KotlinMethod.emitMethodBody(
//        status: KotlinExpression,
//        body: OpenApiBody?,
//        headers: List<OpenApiParameter>
//    ) {
//        var bodyExpression: KotlinExpression = nullLiteral()
//        var mediaTypeExpression: KotlinExpression = nullLiteral()
//
//        if (body != null) {
//            val bodyVariable = "body".variableName()
//            val typeUsage = body.content.typeUsage
//            kotlinParameter(bodyVariable, typeUsage.buildValidType())
//
//            bodyExpression = emitterContext.runEmitter(
//                SerializationStatementEmitter(typeUsage, bodyVariable, body.content.mappedContentType)
//            ).resultStatement
//
//            if (body.content.mappedContentType == ContentType.ApplicationJson) {
//                bodyExpression = bodyExpression.invoke("asString".methodName(), "objectMapper".variableName())
//            }
//
//            mediaTypeExpression = body.content.rawContentType.literal()
//        }
//
//        val headerExpressions = headers.map {
//            kotlinParameter(it.parameterVariableName, it.content.typeUsage.buildValidType())
//            val serializationExpression = emitterContext.runEmitter(
//                SerializationStatementEmitter(
//                    it.content.typeUsage,
//                    it.parameterVariableName,
//                    it.content.mappedContentType
//                )
//            ).resultStatement
//            invoke(Kotlin.PairClass.constructorName, it.name.literal(), serializationExpression)
//        }
//
//        invoke(
//            "status".rawMethodName(),
//            status,
//            mediaTypeExpression,
//            bodyExpression,
//            *headerExpressions.toTypedArray()
//        ).statement()
//    }
//
//    private fun KotlinClass.emitRawHeaderMethods() {
//        // produces:
//        //
//        // fun rawHeaderValue(name: String) = headers.getRequestHeader(name)?.firstOrNull()
//        //
//        //
//        kotlinMethod("rawHeaderValue".methodName(), bodyAsAssignment = true, override = true) {
//            kotlinParameter("name".variableName(), Kotlin.StringClass.typeName())
//
//            "headers".variableName()
//                .invoke("getRequestHeader".methodName(), "name".variableName())
//                .nullCheck()
//                .invoke("firstOrNull".methodName())
//                .statement()
//        }
//
//        // produces:
//        //
//        // fun rawHeaderValues(name: String) = headers.getRequestHeader(name) ?: listOf()
//        kotlinMethod("rawHeaderValues".methodName(), bodyAsAssignment = true, override = true) {
//            kotlinParameter("name".variableName(), Kotlin.StringClass.typeName())
//
//            "headers".variableName()
//                .invoke("getRequestHeader".methodName(), "name".variableName())
//                .nullFallback(invoke("listOf".methodName()))
//                .statement()
//        }
//    }
//
//    private fun KotlinClass.emitInterfaceMembers(request: OpenApiRequest) {
//        // produces:
//        //
//        // override val requestMethod = "<request-method>"
//        // override val requestPath = "<request-path>"
//
//        kotlinMember(
//            "requestMethod".rawVariableName(), type = Kotlin.StringClass.typeName(), accessModifier = null,
//            override = true, initializedInConstructor = false, default = request.method.value.literal()
//        )
//
//        kotlinMember(
//            "requestPath".rawVariableName(), type = Kotlin.StringClass.typeName(), accessModifier = null,
//            override = true, initializedInConstructor = false, default = request.path.literal()
//        )
//    }
}