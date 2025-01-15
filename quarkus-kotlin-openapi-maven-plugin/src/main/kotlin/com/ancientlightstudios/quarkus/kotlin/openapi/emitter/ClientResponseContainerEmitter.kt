package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class ClientResponseContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ClientResponse>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(clientResponse: ClientResponse) {
        kotlinFile(clientResponse.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinInterface(name, sealed = true) {}

            generateHttpResponseClass(clientResponse.httpResponse, name)

            generateErrorResponseInterface(clientResponse.errorResponse, name)
        }
    }

    context(EmitterContext)
    private fun KotlinFile.generateHttpResponseClass(httpResponse: ClientHttpResponse, parent: KotlinTypeName) {
        val httpResponseName = httpResponse.name.asTypeName()
        kotlinClass(httpResponseName, sealed = true, interfaces = listOf(parent.asTypeReference())) {

            kotlinMember("status", Kotlin.Int.asTypeReference(), accessModifier = null, open = true)
            kotlinMember("unsafeBody", Kotlin.Any.asTypeReference().acceptNull(), accessModifier = null, open = true)

            httpResponse.implementations.forEach { implementation ->
                when (val responseCode = implementation.responseCode) {
                    is ResponseCode.Default -> emitDefaultResponseClass(httpResponseName, implementation)
                    is ResponseCode.HttpStatusCode -> emitResponseClass(
                        httpResponseName, responseCode, implementation
                    )
                }
            }
        }
    }

    context(EmitterContext)
    private fun ClassAware.emitDefaultResponseClass(
        parent: KotlinTypeName, implementation: ClientResponseImplementation
    ) {
        val bodyExpression: KotlinExpression = when (val body = implementation.body) {
            null -> nullLiteral()
            else -> body.name.identifier()
        }

        val baseClass = KotlinBaseClass(parent, "status".identifier(), bodyExpression)
        kotlinClass(implementation.name.asTypeName(), baseClass = baseClass) {
            kotlinMember("status", Kotlin.Int.asTypeReference(), accessModifier = null, override = true)

            val context = object : ClientResponseHandlerContext {
                override fun addMember(member: KotlinMember) = this@kotlinClass.addMember(member)
            }

            implementation.headers.forEach { header ->
                getHandler<ClientResponseHandler, Unit> { context.emitHeader(header) }
            }

            implementation.body?.let { body ->
                getHandler<ClientResponseHandler, Unit> { context.emitBody(body) }
            }
        }
    }

    context(EmitterContext)
    private fun ClassAware.emitResponseClass(
        parent: KotlinTypeName, responseCode: ResponseCode.HttpStatusCode, implementation: ClientResponseImplementation
    ) {
        val bodyExpression: KotlinExpression = when (val body = implementation.body) {
            null -> nullLiteral()
            else -> body.name.identifier()
        }

        val status = responseCode.value.literal()
        val baseClass = KotlinBaseClass(parent, status, bodyExpression)
        kotlinClass(implementation.name.asTypeName(), baseClass = baseClass) {

            val context = object : ClientResponseHandlerContext {
                override fun addMember(member: KotlinMember) = this@kotlinClass.addMember(member)
            }

            implementation.headers.forEach { header ->
                getHandler<ClientResponseHandler, Unit> { context.emitHeader(header) }
            }

            implementation.body?.let { body ->
                getHandler<ClientResponseHandler, Unit> { context.emitBody(body) }
            }
        }
    }

    context(EmitterContext)
    private fun KotlinFile.generateErrorResponseInterface(errorResponse: ClientErrorResponse, parent: KotlinTypeName) {
        val errorResponseName = errorResponse.name.asTypeName()
        val interfaces = listOf(parent.asTypeReference(), Library.IsError.asTypeReference())
        kotlinInterface(errorResponseName, sealed = true, interfaces = interfaces) {
            generateErrorClass(
                "RequestErrorTimeout",
                errorResponseName,
                Library.IsTimeoutError,
                "A timeout occurred when communicating with the server.".literal()
            )
            generateErrorClass(
                "RequestErrorUnreachable",
                errorResponseName,
                Library.IsUnreachableError,
                "The server could not be reached.".literal()
            )
            generateErrorClass(
                "RequestErrorConnectionReset",
                errorResponseName,
                Library.IsConnectionResetError,
                "The connection was reset while communicating with the server.".literal()
            )
            generateErrorClass(
                "RequestErrorUnknown",
                errorResponseName,
                Library.IsUnknownError,
                "An unknown error occurred when communicating with the server.".literal()
            ) {
                kotlinMember("cause", Kotlin.Exception.asTypeReference(), accessModifier = null, override = true)
            }

            val responseClass = when (withTestSupport) {
                true -> RestAssured.Response
                false -> Jakarta.Response
            }

            generateErrorClass(
                "ResponseError",
                errorResponseName,
                Library.IsResponseError,
                "reason".identifier()
            ) {
                kotlinMember("reason", Kotlin.String.asTypeReference(), accessModifier = null)
                kotlinMember("response", responseClass.asTypeReference(), accessModifier = null)
            }
        }

    }

    private fun ClassAware.generateErrorClass(
        name: String,
        parent: KotlinTypeName,
        errorInterface: KotlinTypeName,
        message: KotlinExpression,
        block: KotlinClass.() -> Unit = {}
    ) {
        kotlinClass(
            name.asTypeName(),
            baseClass = KotlinBaseClass(parent),
            interfaces = listOf(errorInterface.asTypeReference())
        ) {

            kotlinMember(
                "errorMessage",
                Kotlin.String.asTypeReference(),
                override = true,
                initializedInConstructor = false,
                accessModifier = null,
                default = message
            )

            block()
        }
    }

}

interface ClientResponseHandlerContext : MemberAware {

    /**
     * Generates the standard property for a response header or response body in the client response container.
     * The nullability of the type should reflect the modifications done to the value by the deserialization.
     */
    fun emitProperty(name: String, type: KotlinTypeReference) = kotlinMember(name, type, accessModifier = null)

}

interface ClientResponseHandler : Handler {

    /**
     * Emits the property for a response header in the client response container. The nullability of the type of the
     * property should reflect the modifications done to the value by the deserialization.
     */
    fun ClientResponseHandlerContext.emitHeader(header: ResponseHeader): HandlerResult<Unit>

    /**
     * Emits the property for a response body in the client response container. The nullability of the type of the
     * property should reflect the modifications done to the value by the deserialization.
     */
    fun ClientResponseHandlerContext.emitBody(body: ResponseBody): HandlerResult<Unit>

}
