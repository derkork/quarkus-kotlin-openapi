package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

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
                kotlinMember("dependencyVogel", context.dependencyVogel.name.asTypeReference())

                emitInterfaceMembers(context)
                emitStatusMethod(defaultResponseExists)
                context.methods.forEach { emitStatusMethod(it) }
                emitRawHeaderMethods()
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
            val statement = Misc.ResponseBuilder.identifier()
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
            invoke(Library.RequestHandledSignal.identifier(), statement).throwStatement()
        }
    }

    context(EmitterContext)
    private fun KotlinClass.emitStatusMethod(method: ServerRequestContextResponseMethod) {
        val fromInterface = when (val responseInterface = method.responseInterface) {
            null -> false
            else -> {
                interfaces += responseInterface.name.asTypeReference()
                true
            }
        }

        // produces:
        //
        // [override] fun <name>([status: Int]): Nothing = ...
        kotlinMethod(
            method.name, bodyAsAssignment = true,
            returnType = Kotlin.Nothing.asTypeReference(), override = fromInterface
        ) {
            val codeExpression = when (val code = method.responseCode) {
                is ResponseCode.HttpStatusCode -> code.value.literal()
                is ResponseCode.Default -> {
                    kotlinParameter("status", Kotlin.Int.asTypeReference())
                    "status".identifier()
                }
            }

            var mediaTypeExpression: KotlinExpression = nullLiteral()
            var bodyExpression: KotlinExpression = nullLiteral()

            method.body?.let { body ->
                mediaTypeExpression = body.content.rawContentType.literal()
                bodyExpression = getHandler<ServerRequestContextHandler, KotlinExpression> {
                    emitResponseMethodBody(body, fromInterface, body.content.contentType)
                }
            }

            val headerExpressions = method.headers.map { header ->
                val serializationExpression = getHandler<ServerRequestContextHandler, KotlinExpression> {
                    emitResponseMethodHeader(header, fromInterface, header.content.contentType)
                }
                invoke(Kotlin.Pair.identifier(), header.sourceName.literal(), serializationExpression)
            }

            invoke(
                "status",
                codeExpression,
                mediaTypeExpression,
                bodyExpression,
                *headerExpressions.toTypedArray()
            ).statement()
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

    companion object {
        fun KotlinMethod.emitDefaultResponseMethodHeader(
            name: String, model: ModelUsage, defaultValue: DefaultValue, interfaceMethod: Boolean
        ) {
            // if the method is from a response interface don't repeat the default value here
            kotlinParameter(
                name, model.asTypeReference(), when (interfaceMethod) {
                    true -> null
                    false -> defaultValue.toKotlinExpression()
                }
            )
        }

        fun KotlinMethod.emitDefaultResponseMethodBody(
            name: String, model: ModelUsage, defaultValue: DefaultValue, interfaceMethod: Boolean
        ) {
            // if the method is from a response interface don't repeat the default value here
            kotlinParameter(
                name, model.asTypeReference(), when (interfaceMethod) {
                    true -> null
                    false -> defaultValue.toKotlinExpression()
                }
            )
        }

    }
}

interface ServerRequestContextHandler : Handler {

    fun KotlinMethod.emitResponseMethodHeader(
        header: ServerResponseHeader, fromInterface: Boolean, contentType: ContentType
    ): HandlerResult<KotlinExpression>

    fun KotlinMethod.emitResponseMethodBody(
        body: ServerResponseBody, fromInterface: Boolean, contentType: ContentType
    ): HandlerResult<KotlinExpression>

}
