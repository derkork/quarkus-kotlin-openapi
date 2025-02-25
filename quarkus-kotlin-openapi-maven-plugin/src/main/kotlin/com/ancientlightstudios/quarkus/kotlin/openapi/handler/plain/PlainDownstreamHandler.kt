package com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.variableNameOf

class PlainDownstreamHandler : ServerResponseInterfaceHandler, ServerRequestContextHandler, ClientResponseHandler,
    ClientRestControllerResponseHandler, TestClientRestControllerResponseHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun ServerResponseInterfaceHandlerContext.emitHeader(header: ResponseHeader) =
        header.content.matches(ContentType.TextPlain) { emitProperty(header.name, header.content) }

    override fun ServerResponseInterfaceHandlerContext.emitBody(body: ResponseBody) =
        body.content.matches(ContentType.TextPlain) { emitProperty(body.name, body.content) }

    private fun ServerResponseInterfaceHandlerContext.emitProperty(name: String, content: ContentInfo) {
        val model = content.model
        val defaultValue = model.getDefaultValue()
        emitProperty(name, model.asTypeReference(), defaultValue)
    }

    override fun ServerRequestContextHandlerContext.emitHeader(header: ResponseHeader, fromInterface: Boolean) =
        header.content.matches(ContentType.TextPlain) { emitProperty(header.name, header.content, fromInterface) }

    override fun ServerRequestContextHandlerContext.emitBody(body: ResponseBody, fromInterface: Boolean) =
        body.content.matches(ContentType.TextPlain) { emitProperty(body.name, body.content, fromInterface) }

    private fun ServerRequestContextHandlerContext.emitProperty(
        name: String, content: ContentInfo, fromInterface: Boolean
    ): KotlinExpression {
        val model = content.model
        val defaultValue = model.getDefaultValue()
        emitProperty(name, model.asTypeReference(), defaultValue, fromInterface)

        return registry.getHandler<SerializationHandler, KotlinExpression> {
            serializationExpression(name.identifier(), model, ContentType.TextPlain)
        }
    }

    override fun ClientResponseHandlerContext.emitHeader(header: ResponseHeader) =
        header.content.matches(ContentType.TextPlain) { emitProperty(header.name, header.content) }

    override fun ClientResponseHandlerContext.emitBody(body: ResponseBody) =
        body.content.matches(ContentType.TextPlain) { emitProperty(body.name, body.content) }

    private fun ClientResponseHandlerContext.emitProperty(name: String, content: ContentInfo) {
        val model = content.model
        val defaultValue = model.getDefaultValue()
        val finalModel = model.adjustToDefault(defaultValue)
        emitProperty(name, finalModel.asTypeReference())
    }

    override fun ClientRestControllerResponseHandlerContext.emitHeader(
        header: ResponseHeader, source: KotlinExpression
    ) = header.content.matches(ContentType.TextPlain) {
        // A collection value for plain is always expected to be non-null, because we never get a null value from the
        // framework, and therefor it's not possible to distinguish between a null and empty value.
        val adjustment = when (header.content.model.instance) {
            is CollectionModelInstance -> ::collectionParameterAdjustment
            else -> ::noOpAdjustment
        }

        emitDeserializationStatement(header.context, header.name, source, header.content.model, adjustment)
    }

    override fun ClientRestControllerResponseHandlerContext.emitBody(
        body: ResponseBody, source: KotlinExpression
    ) = body.content.matches(ContentType.TextPlain) {
        // Plain doesn't have a null value, so we have to convert the empty value depending on the target model
        // in case of a string the request container doesn't expect a null value, so null is the same as an empty string.
        // in all other cases, an empty string is the same as null
        val instance = body.content.model.instance
        val adjustment = when {
            instance is PrimitiveTypeModelInstance && instance.itemType == BaseType.String -> ::nullToEmptyBodyAdjustment
            else -> ::emptyBodyToNullAdjustment
        }

        val statement = source.nullCheck().invoke("decodeToString")
        emitDeserializationStatement(body.context, body.name, statement, body.content.model, adjustment)
    }

    private fun collectionParameterAdjustment(
        statement: KotlinExpression, model: ModelUsage
    ) = when (model.isNullable()) {
        true -> {
            // Convert a potential null value into an empty list and change the usage to be not nullable to satisfy the compiler
            val resultStatement = statement.invoke("nullAsEmptyList")
            val finalModel = model.rejectNull()
            resultStatement to finalModel
        }

        else -> noOpAdjustment(statement, model)
    }

    private fun emptyBodyToNullAdjustment(
        statement: KotlinExpression, model: ModelUsage
    ): Pair<KotlinExpression, ModelUsage> {
        // Convert an empty string into a null value
        val resultStatement = statement.invoke("emptyStringAsNull")
        return resultStatement to model
    }

    private fun nullToEmptyBodyAdjustment(
        statement: KotlinExpression, model: ModelUsage
    ) = when (model.isNullable()) {
        true -> {
            // Convert a potential null value into an empty string and change the usage to be not nullable to satisfy the compiler
            val resultStatement = statement.invoke("nullAsEmptyString")
            val finalModel = model.rejectNull()
            resultStatement to finalModel
        }

        else -> noOpAdjustment(statement, model)
    }

    private fun noOpAdjustment(statement: KotlinExpression, model: ModelUsage) = statement to model

    private fun StatementAware.emitDeserializationStatement(
        context: String, inputName: String, input: KotlinExpression, model: ModelUsage,
        adjustment: (KotlinExpression, ModelUsage) -> Pair<KotlinExpression, ModelUsage>
    ): InstantiationParameter {
        // Maybe.Success(<context>, <parameterName>)
        val statement = invoke(Library.MaybeSuccess.identifier(), context.literal(), input).wrap()

        val (adjustedStatement, finalModel) = adjustment(statement, model)

        val maybe = registry.getHandler<DeserializationHandler, KotlinExpression> {
            deserializationExpression(adjustedStatement, finalModel, ContentType.TextPlain)
        }.declaration(variableNameOf(inputName, "Maybe"))

        return MaybeParameter(maybe)
    }

    override fun TestClientRestControllerResponseHandlerContext.emitHeader(
        header: ResponseHeader, source: KotlinExpression
    ) = header.content.matches(ContentType.TextPlain) {
        // A collection value for plain is always expected to be non-null, because we never get a null value from the
        // framework, and therefor it's not possible to distinguish between a null and empty value.
        val adjustment = when (header.content.model.instance) {
            is CollectionModelInstance -> ::collectionParameterAdjustment
            else -> ::noOpAdjustment
        }

        emitDeserializationStatement(header.context, header.name, source, header.content.model, adjustment)
    }

    override fun TestClientRestControllerResponseHandlerContext.emitBody(
        body: ResponseBody, source: KotlinExpression
    ) = body.content.matches(ContentType.TextPlain) {
        val statement = source.invoke("asString")
        // Plain doesn't have a null value, so we have to convert the empty value depending on the target model
        // in case of a string the request container doesn't expect a null value, so null is the same as an empty string.
        // in all other cases, an empty string is the same as null
        val instance = body.content.model.instance
        val adjustment = when {
            instance is PrimitiveTypeModelInstance && instance.itemType == BaseType.String -> ::nullToEmptyBodyAdjustment
            else -> ::emptyBodyToNullAdjustment
        }

        emitDeserializationStatement(body.context, body.name, statement, body.content.model, adjustment)
    }

    // default value for a response value can be different to the default value of a request value
    private fun ModelUsage.getDefaultValue(): DefaultValue = when (instance) {
        is CollectionModelInstance -> DefaultValue.EmptyList
        is EnumModelInstance -> when (instance.defaultValue) {
            null -> DefaultValue.nullOrNone(isNullable())
            else -> DefaultValue.EnumValue(instance.ref, instance.defaultValue)
        }

        is MapModelInstance -> DefaultValue.EmptyMap
        is ObjectModelInstance,
        is OneOfModelInstance -> DefaultValue.None // incompatible with plain
        is PrimitiveTypeModelInstance -> when (instance.defaultValue) {
            null -> DefaultValue.nullOrNone(isNullable())
            else -> DefaultValue.StaticValue(instance.itemType, instance.defaultValue)
        }
    }
}

