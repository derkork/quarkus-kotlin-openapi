package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContextResponseMethod

class ServerRequestContextEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerRequestContext>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(context: ServerRequestContext) {
        kotlinFile(context.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinClass(name, interfaces = listOf(Library.RequestContext.asTypeReference())) {
                val defaultResponseExists = context.methods.any { it.responseCode == ResponseCode.Default }
                if (!defaultResponseExists) {
                    interfaces += Library.ResponseWithGenericStatus.asTypeReference()
                }

                if (context.container != null) {
                    val containerType = Library.Maybe.asTypeReference(context.container.name.asTypeReference())
                    kotlinMember("request", containerType, accessModifier = null)
                }

                kotlinMember("headers", Jakarta.HttpHeaders.asTypeReference())
                // TODO
//            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())

                emitInterfaceMembers(context)
                emitRawHeaderMethods()
                emitStatusMethod(defaultResponseExists)
                context.methods.forEach { emitStatusMethod(it) }
            }
        }
    }

    private fun KotlinClass.emitStatusMethod(defaultResponseExists: Boolean) {
        val accessModifier = when (defaultResponseExists) {
            true -> KotlinAccessModifier.Private
            false -> null
        }

        val parameterDefaultValue = when (defaultResponseExists) {
            true -> nullLiteral()
            false -> null
        }

        // produces:
        //
        // <override|private> fun status(status: Int, mediaType: String? [= null], body: Any? [= null], vararg headers: Pair<String, Any?>): Nothing = ...
        kotlinMethod(
            "status", bodyAsAssignment = true, accessModifier = accessModifier,
            returnType = Kotlin.Nothing.asTypeReference(), override = !defaultResponseExists
        ) {
            kotlinParameter("status", Kotlin.Int.asTypeReference())
            kotlinParameter("mediaType", Kotlin.String.asTypeReference().nullable(), parameterDefaultValue)
            kotlinParameter("body", Kotlin.Any.asTypeReference().nullable(), parameterDefaultValue)
            kotlinParameter(
                "headers", Kotlin.Pair.asTypeReference(
                    Kotlin.String.asTypeReference(), Kotlin.Any.asTypeReference().nullable()
                ), asParameterList = true
            )

            // produces:
            //
            // throw RequestHandledSignal(RestResponse.ResponseBuilder.create<Any?>(status)
            //     .entity(body)
            //     .type(mediaType)
            //     .apply {
            //         headers.forEach { headers(it.first, it.second) }
            //     }.build())
            val statement = Misc.ResponseBuilder
                .invoke("create", "status".identifier(), genericTypes = listOf(Kotlin.Any.asTypeReference().nullable()))
                .wrap()
                .invoke("entity", "body".identifier())
                .wrap()
                .invoke("type", "mediaType".identifier())
                .wrap()
                .invoke("apply") {
                    "headers".identifier().invoke("forEach") {
                        invoke(
                            "headers",
                            "it".identifier().property("first"),
                            "it".identifier().property("second")
                        ).statement()
                    }.statement()
                }
                .invoke("build")
            invoke(Library.RequestHandledSignal, statement).throwStatement()
        }
    }

    private fun KotlinClass.emitStatusMethod(method: ServerRequestContextResponseMethod) {
        method.responseInterface?.let { interfaces += it.name.asTypeReference() }

        // produces:
        //
        // [override] fun <name>([status: Int]): Nothing = ...
        kotlinMethod(
            method.name, bodyAsAssignment = true,
            returnType = Kotlin.Nothing.asTypeReference(), override = method.responseInterface != null
        ) {
            val codeExpression = when (val code = method.responseCode) {
                is ResponseCode.HttpStatusCode -> code.value.literal()
                is ResponseCode.Default -> {
                    kotlinParameter("status", Kotlin.Int.asTypeReference())
                    "status".identifier()
                }
            }

            // TODO: proper body serialization
            codeExpression.statement()
        }
    }


    private fun KotlinClass.emitInterfaceMembers(context: ServerRequestContext) {
        // produces:
        //
        // override val requestMethod: String = "<request-method>"
        kotlinMember(
            "requestMethod", type = Kotlin.String.asTypeReference(), accessModifier = null,
            override = true, initializedInConstructor = false, default = context.restMethod.value.literal()
        )

        // produces:
        //
        // override val requestPath: String = "<request-path>"
        kotlinMember(
            "requestPath", type = Kotlin.String.asTypeReference(), accessModifier = null,
            override = true, initializedInConstructor = false, default = context.restPath.literal()
        )
    }

    private fun KotlinClass.emitRawHeaderMethods() {
        // produces:
        //
        // fun rawHeaderValue(name: String) = headers.getRequestHeader(name)?.firstOrNull()
        kotlinMethod("rawHeaderValue", bodyAsAssignment = true, override = true) {
            kotlinParameter("name", Kotlin.String.asTypeReference())

            "headers".identifier()
                .invoke("getRequestHeader", "name".identifier())
                .nullCheck()
                .invoke("firstOrNull")
                .statement()
        }

        // produces:
        //
        // fun rawHeaderValues(name: String) = headers.getRequestHeader(name) ?: listOf()
        kotlinMethod("rawHeaderValues", bodyAsAssignment = true, override = true) {
            kotlinParameter("name", Kotlin.String.asTypeReference())

            "headers".identifier()
                .invoke("getRequestHeader", "name".identifier())
                .nullFallback(invoke("listOf"))
                .statement()
        }
    }

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

}