package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class OctetUpstreamHandler : ServerRestControllerHandler, ServerRequestContainerHandler, ClientDelegateHandler,
    ClientRestControllerRequestHandler, TestClientRequestBuilderHandler, TestClientRestControllerHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun ServerRestControllerHandlerContext.emitParameter(parameter: RequestParameter):
            HandlerResult<InstantiationParameter> = parameter.content.matches(ContentType.ApplicationOctetStream) {
        SpecIssue("Octet encoding not supported for request parameter")
    }

    override fun ServerRestControllerHandlerContext.emitBody(body: RequestBody): HandlerResult<InstantiationParameter> =
        body.content.matches(ContentType.ApplicationOctetStream) {
            emitProperty(body.name, Kotlin.ByteArray.asTypeReference().acceptNull())

            // Maybe.Success(<context>, <parameterName>)
            var statement = invoke(Library.MaybeSuccess.identifier(), body.context.literal(), body.name.identifier())
                .wrap()

            // The body value for octet is always expected to be non-null by the request container, because it's not
            // possible to distinguish between a null and empty payload. Therefore, we convert a potential null value into
            // an empty array and change the usage to be not nullable to satisfy the compiler
            statement = statement.invoke("nullAsEmptyArray")
            val finalModel = body.content.model.rejectNull()

            val maybe = registry.getHandler<DeserializationHandler, KotlinExpression> {
                deserializationExpression(statement, finalModel, ContentType.ApplicationOctetStream)
            }.declaration("bodyMaybe")

            MaybeParameter(maybe)
        }

    override fun ServerRequestContainerHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit> =
        parameter.content.matches(ContentType.ApplicationOctetStream) {
            SpecIssue("Octet encoding not supported for response header")
        }

    override fun ServerRequestContainerHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationOctetStream) {
            val model = body.content.model
            val defaultValue = model.getDefaultValue()
            emitProperty(body.name, model.adjustToDefault(defaultValue).asTypeReference())
        }

    override fun ClientDelegateHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit> =
        parameter.content.matches(ContentType.ApplicationOctetStream) {
            SpecIssue("Octet encoding not supported for request parameter")
        }

    override fun ClientDelegateHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationOctetStream) {
            emitProperty(body.name, Kotlin.ByteArray.asTypeReference())
        }

    override fun ClientRestControllerRequestHandlerContext.emitParameter(parameter: RequestParameter):
            HandlerResult<List<KotlinExpression>> =
        parameter.content.matches(ContentType.ApplicationOctetStream) {
            SpecIssue("Octet encoding not supported for request parameter")
        }

    override fun ClientRestControllerRequestHandlerContext.emitBody(body: RequestBody):
            HandlerResult<List<KotlinExpression>> = body.content.matches(ContentType.ApplicationOctetStream) {
        val model = body.content.model
        val typeInfo = body.content.forUserProvidedValue()
        emitProperty(body.name, typeInfo.type, typeInfo.defaultValue)

        val statement = registry.getHandler<SerializationHandler, KotlinExpression> {
            serializationExpression(body.name.identifier(), model, ContentType.ApplicationOctetStream)
        }

        listOf(statement)
    }

    override fun TestClientRequestBuilderHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit> =
        parameter.content.matches(ContentType.ApplicationOctetStream) {
            SpecIssue("Octet encoding not supported for request parameter")
        }

    override fun TestClientRequestBuilderHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationOctetStream) {
            val model = body.content.model.rejectNull()
            val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression("value".identifier(), model, ContentType.ApplicationOctetStream)
            }

            emitDefaultBody(body, Kotlin.ByteArray.asTypeReference().acceptNull(), serialization)
        }

    override fun TestClientRestControllerHandlerContext.parameterType(parameter: RequestParameter):
            HandlerResult<TypeInfo> = parameter.content.matches(ContentType.ApplicationOctetStream) {
        SpecIssue("Octet encoding not supported for request parameter")
    }

    override fun TestClientRestControllerHandlerContext.bodyType(body: RequestBody) =
        body.content.matches(ContentType.ApplicationOctetStream) { body.content.forUserProvidedValue() }

    override fun TestClientRestControllerHandlerContext.pathParameterSerialization(parameter: RequestParameter):
            HandlerResult<KotlinExpression> = parameter.content.matches(ContentType.ApplicationOctetStream) {
        SpecIssue("Octet encoding not supported for request parameter")
    }

    // default value for a request value can be different to the default value of a response value
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

    // don't adjust the model to the default value, to keep nullability even if there is a default value available
    private fun ContentInfo.forUserProvidedValue() = TypeInfo(model.asTypeReference(), model.getDefaultValue())

}