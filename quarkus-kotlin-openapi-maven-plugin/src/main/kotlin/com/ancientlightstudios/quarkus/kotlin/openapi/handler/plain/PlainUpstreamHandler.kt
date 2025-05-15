package com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.variableNameOf

class PlainUpstreamHandler : ServerRestControllerHandler, ServerRequestContainerHandler, ClientDelegateHandler,
    ClientRestControllerRequestHandler, TestClientRequestBuilderHandler, TestClientRestControllerHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun ServerRestControllerHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.TextPlain) {
            emitProperty(
                parameter.name,
                // always nullable on the server side
                parameter.content.model.getSerializedType(true),
                getSourceAnnotation(parameter.kind, parameter.sourceName)
            )

            // A collection value for plain is always expected to be non-null by the request container, because we never
            // get a null value from the framework, and therefor it's not possible to distinguish between a null and
            // empty value.
            val adjustment = when (parameter.content.model.instance) {
                is CollectionModelInstance -> ::collectionParameterAdjustment
                else -> ::noOpAdjustment
            }

            emitDeserializationStatement(parameter.context, parameter.name, parameter.content.model, adjustment)
        }

    override fun ServerRestControllerHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.TextPlain) {
            // Plain doesn't have a null value, so we have to convert the empty value depending on the target model
            // in case of a string the request container doesn't expect a null value, so null is the same as an empty string.
            // in all other cases, an empty string is the same as null
            val instance = body.content.model.instance
            val adjustment = when {
                instance is PrimitiveTypeModelInstance && instance.itemType == BaseType.String -> ::nullToEmptyBodyAdjustment
                else -> ::emptyBodyToNullAdjustment
            }

            // always nullable on the server side
            emitProperty(body.name, body.content.model.getSerializedType(true))
            emitDeserializationStatement(body.context, body.name, body.content.model, adjustment)
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

    private fun ServerRestControllerHandlerContext.emitDeserializationStatement(
        context: String, inputName: String, model: ModelUsage,
        adjustment: (KotlinExpression, ModelUsage) -> Pair<KotlinExpression, ModelUsage>
    ): InstantiationParameter {
        // Maybe.Success(<context>, <parameterName>)
        val statement = invoke(Library.MaybeSuccess.identifier(), context.literal(), inputName.identifier()).wrap()

        val (adjustedStatement, finalModel) = adjustment(statement, model)

        val maybe = registry.getHandler<DeserializationHandler, KotlinExpression> {
            deserializationExpression(adjustedStatement, finalModel, ContentType.TextPlain)
        }.declaration(variableNameOf(inputName, "Maybe"))

        return MaybeParameter(maybe)
    }

    override fun ServerRequestContainerHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.TextPlain) { emitProperty(parameter.name, parameter.content) }

    override fun ServerRequestContainerHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.TextPlain) { emitProperty(body.name, body.content) }

    private fun ServerRequestContainerHandlerContext.emitProperty(name: String, content: ContentInfo) {
        val model = content.model
        val defaultValue = model.getDefaultValue()
        emitProperty(name, model.adjustToDefault(defaultValue).asTypeReference())
    }

    override fun ClientDelegateHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.TextPlain) {
            emitProperty(parameter.name, parameter.content, getSourceAnnotation(parameter.kind, parameter.sourceName))
        }

    override fun ClientDelegateHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.TextPlain) { emitProperty(body.name, body.content, null) }

    private fun ClientDelegateHandlerContext.emitProperty(
        name: String, content: ContentInfo, annotation: KotlinAnnotation?
    ) {
        emitProperty(name, content.model.getSerializedType(), annotation)
    }

    override fun ClientRestControllerRequestHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.TextPlain) {
            // A collection value for plain is always expected to be non-null. Because the framework doesn't distinguish
            // between a null and empty value.
            val adjustment = when (parameter.content.model.instance) {
                is CollectionModelInstance -> ::collectionParameterAdjustment
                else -> ::noOpAdjustment
            }

            emitProperty(parameter.name, parameter.content, adjustment)
        }

    override fun ClientRestControllerRequestHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.TextPlain) {
            // Plain doesn't have a null value, so we have to convert the empty value depending on the target model
            // in case of a string the framework doesn't expect a null value, so null is the same as an empty string.
            // in all other cases, an empty string is the same as null
            val instance = body.content.model.instance
            val adjustment = when {
                instance is PrimitiveTypeModelInstance && instance.itemType == BaseType.String -> ::nullToEmptyBodyAdjustment
                else -> ::noOpAdjustment
            }

            emitProperty(body.name, body.content, adjustment)
        }

    private fun ClientRestControllerRequestHandlerContext.emitProperty(
        name: String, content: ContentInfo,
        adjustment: (KotlinExpression, ModelUsage) -> Pair<KotlinExpression, ModelUsage>
    ): List<KotlinExpression> {
        val typeInfo = content.forUserProvidedValue()
        emitProperty(name, typeInfo.type, typeInfo.defaultValue)

        val (adjustedStatement, finalModel) = adjustment(name.identifier(), content.model)

        val payload = registry.getHandler<SerializationHandler, KotlinExpression> {
            serializationExpression(adjustedStatement, finalModel, ContentType.TextPlain)
        }
            .declaration(variableNameOf(name, "Payload"))

        return listOf(payload.identifier())
    }

    override fun TestClientRequestBuilderHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.TextPlain) {
            val model = parameter.content.model.rejectNull()
            val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression("value".identifier(), model, ContentType.TextPlain)
            }

            emitDefaultParameter(parameter, model.asTypeReference().acceptNull(), serialization)
        }

    override fun TestClientRequestBuilderHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.TextPlain) {
            val model = body.content.model.rejectNull()
            val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression("value".identifier(), model, ContentType.TextPlain)
            }

            emitDefaultBody(body, model.asTypeReference().acceptNull(), serialization)
        }

    override fun TestClientRestControllerHandlerContext.parameterType(parameter: RequestParameter) =
        parameter.content.matches(ContentType.TextPlain) { parameter.content.forUserProvidedValue() }

    override fun TestClientRestControllerHandlerContext.bodyType(body: RequestBody) =
        body.content.matches(ContentType.TextPlain) { body.content.forUserProvidedValue() }

    override fun TestClientRestControllerHandlerContext.pathParameterSerialization(parameter: RequestParameter) =
        parameter.content.matches(ContentType.TextPlain) {
            val model = parameter.content.model
            registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression(parameter.name.identifier(), model, ContentType.TextPlain)
            }
        }

    // default value for a request value can be different to the default value of a response value
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

    private fun ModelUsage.getSerializedType(forceNullable: Boolean = false): KotlinTypeReference {
        return when (val instance = this.instance) {
            is CollectionModelInstance -> Kotlin.List.asTypeReference(instance.items.getSerializedType(forceNullable))
            else -> Kotlin.String.asTypeReference()
        }.run {
            when (forceNullable || isNullable()) {
                true -> acceptNull()
                false -> this
            }
        }
    }

    // don't adjust the model to the default value, to keep nullability even if there is a default value available
    private fun ContentInfo.forUserProvidedValue() = TypeInfo(model.asTypeReference(), model.getDefaultValue())

}