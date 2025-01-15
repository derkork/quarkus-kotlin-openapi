package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Library
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.literal
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.wrap
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.variableNameOf
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class OctetDownstreamHandler : ServerResponseInterfaceHandler, ServerRequestContextHandler, ClientResponseHandler,
    ClientRestControllerResponseHandler, TestClientRestControllerResponseHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun ServerResponseInterfaceHandlerContext.emitHeader(header: ResponseHeader): HandlerResult<Unit> =
        header.content.matches(ContentType.ApplicationOctetStream) {
            SpecIssue("Octet encoding not supported for response header")
        }

    override fun ServerResponseInterfaceHandlerContext.emitBody(body: ResponseBody) =
        body.content.matches(ContentType.ApplicationOctetStream) {
            val model = body.content.model
            val defaultValue = model.getDefaultValue()
            emitProperty(body.name, model.asTypeReference(), defaultValue)
        }

    override fun ServerRequestContextHandlerContext.emitHeader(header: ResponseHeader, fromInterface: Boolean):
            HandlerResult<KotlinExpression> = header.content.matches(ContentType.ApplicationOctetStream) {
        SpecIssue("Octet encoding not supported for response header")
    }

    override fun ServerRequestContextHandlerContext.emitBody(body: ResponseBody, fromInterface: Boolean) =
        body.content.matches(ContentType.ApplicationOctetStream) {
            val model = body.content.model
            val defaultValue = model.getDefaultValue()
            emitProperty(body.name, model.asTypeReference(), defaultValue, fromInterface)

            registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression(body.name.identifier(), model, ContentType.ApplicationOctetStream)
            }
        }

    override fun ClientResponseHandlerContext.emitHeader(header: ResponseHeader): HandlerResult<Unit> =
        header.content.matches(ContentType.ApplicationOctetStream) {
            SpecIssue("Octet encoding not supported for response header")
        }

    override fun ClientResponseHandlerContext.emitBody(body: ResponseBody) =
        body.content.matches(ContentType.ApplicationOctetStream) {
            val model = body.content.model
            val defaultValue = model.getDefaultValue();
            val finalModel = model.adjustToDefault(defaultValue)
            emitProperty(body.name, finalModel.asTypeReference())
        }

    override fun ClientRestControllerResponseHandlerContext.emitHeader(
        header: ResponseHeader, source: KotlinExpression
    ): HandlerResult<InstantiationParameter> =
        header.content.matches(ContentType.ApplicationOctetStream) {
            SpecIssue("Octet encoding not supported for response header")
        }

    override fun ClientRestControllerResponseHandlerContext.emitBody(
        body: ResponseBody, source: KotlinExpression
    ): HandlerResult<InstantiationParameter> = body.content.matches(ContentType.ApplicationOctetStream) {
        // Maybe.Success(<context>, <parameterName>)
        var statement =
            invoke(Library.MaybeSuccess.identifier(), "response.${body.sourceName}".literal(), source).wrap()

        // The body value for octet is always expected to be non-null by the request container, because it's not
        // possible to distinguish between a null and empty payload. Therefore, we convert a potential null value into
        // an empty array and change the usage to be not nullable to satisfy the compiler
        statement = statement.invoke("nullAsEmptyArray")
        val finalModel = body.content.model.rejectNull()

        val maybe = registry.getHandler<DeserializationHandler, KotlinExpression> {
            deserializationExpression(statement, finalModel, ContentType.ApplicationOctetStream)
        }.declaration(variableNameOf(body.sourceName, "Maybe"))

        MaybeParameter(maybe)
    }

    override fun TestClientRestControllerResponseHandlerContext.emitHeader(
        header: ResponseHeader, source: KotlinExpression
    ): HandlerResult<InstantiationParameter> =
        header.content.matches(ContentType.ApplicationOctetStream) {
            SpecIssue("Octet encoding not supported for response header")
        }

    override fun TestClientRestControllerResponseHandlerContext.emitBody(
        body: ResponseBody, source: KotlinExpression
    ): HandlerResult<InstantiationParameter> = body.content.matches(ContentType.ApplicationOctetStream) {
        // Maybe.Success(<context>, <parameterName>)
        val statement = invoke(
            Library.MaybeSuccess.identifier(),
            "response.${body.sourceName}".literal(),
            source.invoke("asByteArray")
        ).wrap()

        val finalModel = body.content.model.rejectNull()

        val maybe = registry.getHandler<DeserializationHandler, KotlinExpression> {
            deserializationExpression(statement, finalModel, ContentType.ApplicationOctetStream)
        }.declaration(variableNameOf(body.sourceName, "Maybe"))

        MaybeParameter(maybe)
    }

    // default value for a response value can be different to the default value of a request value
    private fun ModelUsage.getDefaultValue(): DefaultValue = when (instance) {
        is CollectionModelInstance,
        is EnumModelInstance,
        is MapModelInstance,
        is ObjectModelInstance,
        is OneOfModelInstance -> DefaultValue.None // incompatible with octet
        is PrimitiveTypeModelInstance -> when (instance.itemType) {
            is BaseType.ByteArray -> DefaultValue.EmptyByteArray
            else -> DefaultValue.None // incompatible with octet
        }
    }
}