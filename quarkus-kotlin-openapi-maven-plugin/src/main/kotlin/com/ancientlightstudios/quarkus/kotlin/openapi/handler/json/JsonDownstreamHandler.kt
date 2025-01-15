package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.variableNameOf

class JsonDownstreamHandler : ServerResponseInterfaceHandler, ServerRequestContextHandler, ClientResponseHandler,
    ClientRestControllerResponseHandler, TestClientRestControllerResponseHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun ServerResponseInterfaceHandlerContext.emitHeader(header: ResponseHeader) =
        header.content.matches(ContentType.ApplicationJson) { emitProperty(header.name, header.content) }

    override fun ServerResponseInterfaceHandlerContext.emitBody(body: ResponseBody) =
        body.content.matches(ContentType.ApplicationJson) { emitProperty(body.name, body.content) }

    private fun ServerResponseInterfaceHandlerContext.emitProperty(name: String, content: ContentInfo) {
        val model = content.model
        emitProperty(name, model.asTypeReference(), model.getDefaultValue())
    }

    override fun ServerRequestContextHandlerContext.emitHeader(header: ResponseHeader, fromInterface: Boolean) =
        header.content.matches(ContentType.ApplicationJson) { emitProperty(header.name, header.content, fromInterface) }

    override fun ServerRequestContextHandlerContext.emitBody(body: ResponseBody, fromInterface: Boolean) =
        body.content.matches(ContentType.ApplicationJson) { emitProperty(body.name, body.content, fromInterface) }

    private fun ServerRequestContextHandlerContext.emitProperty(
        name: String, content: ContentInfo, fromInterface: Boolean
    ): KotlinExpression {
        val model = content.model
        val defaultValue = model.getDefaultValue()
        emitProperty(name, model.asTypeReference(), defaultValue, fromInterface)

        val expression = registry.getHandler<SerializationHandler, KotlinExpression> {
            serializationExpression(name.identifier(), model, ContentType.ApplicationJson)
        }
        return expression.invoke("asString", "dependencyVogel".identifier().property("objectMapper"))
    }

    override fun ClientResponseHandlerContext.emitHeader(header: ResponseHeader) =
        header.content.matches(ContentType.ApplicationJson) { emitProperty(header.name, header.content) }

    override fun ClientResponseHandlerContext.emitBody(body: ResponseBody) =
        body.content.matches(ContentType.ApplicationJson) { emitProperty(body.name, body.content) }

    private fun ClientResponseHandlerContext.emitProperty(name: String, content: ContentInfo) {
        val model = content.model
        val defaultValue = model.getDefaultValue()
        val finalModel = model.adjustToDefault(defaultValue)
        emitProperty(name, finalModel.asTypeReference())
    }

    override fun ClientRestControllerResponseHandlerContext.emitHeader(
        header: ResponseHeader, source: KotlinExpression
    ) = header.content.matches(ContentType.ApplicationJson) {
        emitDeserializationStatement(
            "response.${header.kind.value}.${header.sourceName}", header.name, source, header.content.model
        )
    }

    override fun ClientRestControllerResponseHandlerContext.emitBody(
        body: ResponseBody, source: KotlinExpression
    ) = body.content.matches(ContentType.ApplicationJson) {
        val statement = source.nullCheck().invoke("decodeToString")
        emitDeserializationStatement(
            "response.${body.sourceName}", body.name, statement, body.content.model
        )
    }

    private fun StatementAware.emitDeserializationStatement(
        context: String, inputName: String, input: KotlinExpression, model: ModelUsage
    ): InstantiationParameter {
        // Maybe.Success(<context>, <parameterName>)
        val statement = invoke(Library.MaybeSuccess.identifier(), context.literal(), input).wrap()
            .invoke("asJson", "dependencyVogel".identifier().property("objectMapper")).wrap()

        val maybe = registry.getHandler<DeserializationHandler, KotlinExpression> {
            deserializationExpression(statement, model, ContentType.ApplicationJson)
        }.declaration(variableNameOf(inputName, "Maybe"))

        return MaybeParameter(maybe)
    }

    override fun TestClientRestControllerResponseHandlerContext.emitHeader(
        header: ResponseHeader, source: KotlinExpression
    ) = header.content.matches(ContentType.ApplicationJson) {
        emitDeserializationStatement(
            "response.${header.kind.value}.${header.sourceName}", header.name, source, header.content.model
        )
    }

    override fun TestClientRestControllerResponseHandlerContext.emitBody(
        body: ResponseBody, source: KotlinExpression
    ) = body.content.matches(ContentType.ApplicationJson) {
        val statement = source.invoke("asString")
        emitDeserializationStatement(
            "response.${body.sourceName}", body.name, statement, body.content.model
        )
    }

    // default value for a response value can be different to the default value of a request value
    private fun ModelUsage.getDefaultValue(): DefaultValue = when (instance) {
        is CollectionModelInstance -> DefaultValue.nullOr(DefaultValue.EmptyList, isNullable())
        is EnumModelInstance -> when (instance.defaultValue) {
            null -> DefaultValue.nullOrNone(isNullable())
            else -> DefaultValue.EnumValue(instance.ref, instance.defaultValue)
        }

        is MapModelInstance -> DefaultValue.nullOr(DefaultValue.EmptyMap, isNullable())
        is ObjectModelInstance -> DefaultValue.nullOrNone(isNullable())
        is OneOfModelInstance -> DefaultValue.nullOrNone(isNullable())
        is PrimitiveTypeModelInstance -> when (instance.defaultValue) {
            null -> DefaultValue.nullOrNone(isNullable())
            else -> DefaultValue.StaticValue(instance.itemType, instance.defaultValue)
        }
    }
}