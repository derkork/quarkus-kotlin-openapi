package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.variableNameOf

class JsonUpstreamHandler : ServerRestControllerHandler, ServerRequestContainerHandler, ClientDelegateHandler,
    ClientRestControllerRequestHandler, TestClientRequestBuilderHandler, TestClientRestControllerHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun ServerRestControllerHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.ApplicationJson) {
            val context = "request.${parameter.kind.value}.${parameter.sourceName}"
            val annotation = getSourceAnnotation(parameter.kind, parameter.sourceName)
            emitProperty(parameter.name, parameter.content, context, annotation)
        }

    override fun ServerRestControllerHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationJson) {
            val context = "request.${body.name}"
            emitProperty(body.name, body.content, context, null)
        }

    private fun ServerRestControllerHandlerContext.emitProperty(
        name: String, content: ContentInfo, context: String, annotation: KotlinAnnotation?
    ): InstantiationParameter {
        emitProperty(name, Kotlin.String.asTypeReference().acceptNull(), annotation)
        return emitDeserializationStatement(context, name, content.model)
    }

    private fun ServerRestControllerHandlerContext.emitDeserializationStatement(
        context: String, inputName: String, model: ModelUsage
    ): InstantiationParameter {
        // Maybe.Success(<context>, <parameterName>)
        val statement = invoke(Library.MaybeSuccess.identifier(), context.literal(), inputName.identifier()).wrap()
            .invoke("asJson", "dependencyVogel".identifier().property("objectMapper")).wrap()

        val maybe = registry.getHandler<DeserializationHandler, KotlinExpression> {
            deserializationExpression(statement, model, ContentType.ApplicationJson)
        }.declaration(variableNameOf(inputName, "Maybe"))

        return MaybeParameter(maybe)
    }

    override fun ServerRequestContainerHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.ApplicationJson) { emitProperty(parameter.name, parameter.content) }

    override fun ServerRequestContainerHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationJson) { emitProperty(body.name, body.content) }

    private fun ServerRequestContainerHandlerContext.emitProperty(name: String, content: ContentInfo) {
        val model = content.model
        val defaultValue = model.getDefinedDefaultValue()
        emitProperty(name, model.adjustToDefault(defaultValue).asTypeReference())
    }

    override fun ClientDelegateHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.ApplicationJson) {
            emitProperty(
                parameter.name,
                Kotlin.String.asTypeReference().acceptNull(),
                getSourceAnnotation(parameter.kind, parameter.sourceName)
            )
        }

    override fun ClientDelegateHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationJson) {
            emitProperty(body.name, Kotlin.String.asTypeReference().acceptNull())
        }

    override fun ClientRestControllerRequestHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.ApplicationJson) { emitProperty(parameter.name, parameter.content) }

    override fun ClientRestControllerRequestHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationJson) { emitProperty(body.name, body.content) }

    private fun ClientRestControllerRequestHandlerContext.emitProperty(name: String, content: ContentInfo):
            List<KotlinExpression> {
        val typeInfo = content.forUserProvidedValue()
        emitProperty(name, typeInfo.type, typeInfo.defaultValue)

        val payload = registry.getHandler<SerializationHandler, KotlinExpression> {
            serializationExpression(name.identifier(), content.model, ContentType.ApplicationJson)
        }
            .invoke("asString", "dependencyVogel".identifier().property("objectMapper"))
            .declaration(variableNameOf(name, "Payload"))

        return listOf(payload.identifier())
    }

    override fun TestClientRequestBuilderHandlerContext.emitParameter(parameter: RequestParameter) =
        parameter.content.matches(ContentType.ApplicationJson) {
            val model = parameter.content.model.rejectNull()
            val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression("value".identifier(), model, ContentType.ApplicationJson)
            }.invoke("asString", "dependencyVogel".identifier().property("objectMapper"))

            emitDefaultParameter(parameter, model.asTypeReference().acceptNull(), serialization)
        }

    override fun TestClientRequestBuilderHandlerContext.emitBody(body: RequestBody) =
        body.content.matches(ContentType.ApplicationJson) {
            val model = body.content.model.rejectNull()
            val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression("value".identifier(), model, ContentType.ApplicationJson)
            }.invoke("asString", "dependencyVogel".identifier().property("objectMapper"))

            emitDefaultBody(body, model.asTypeReference().acceptNull(), serialization)

            val unsafeJsonBody = when (model.instance) {
                is CollectionModelInstance,
                is MapModelInstance,
                is ObjectModelInstance,
                is OneOfModelInstance -> true

                is EnumModelInstance,
                is PrimitiveTypeModelInstance -> false
            }

            if (unsafeJsonBody) {
                emitUnsafeBody(body)
            }
        }

    private fun TestClientRequestBuilderHandlerContext.emitUnsafeBody(body: RequestBody) {
        val model = body.content.model.rejectNull()
        val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
            serializationExpression("value".identifier(), model, ContentType.ApplicationJson)
        }.invoke("asString", "dependencyVogel".identifier().property("objectMapper"))

        // this method needs a special annotation
        val nestedContext = object : TestClientRequestBuilderHandlerContext {

            override fun addMethod(method: KotlinMethod) {
                method.kotlinAnnotation(Kotlin.JvmName, "name" to "bodyWithUnsafe".literal())
                this@emitUnsafeBody.addMethod(method)
            }

        }

        nestedContext.emitDefaultBody(body, model.asUnsafeTypeReference(), serialization)
    }

    override fun TestClientRestControllerHandlerContext.parameterType(parameter: RequestParameter) =
        parameter.content.matches(ContentType.ApplicationJson) { parameter.content.forUserProvidedValue() }

    override fun TestClientRestControllerHandlerContext.bodyType(body: RequestBody) =
        body.content.matches(ContentType.ApplicationJson) { body.content.forUserProvidedValue() }

    override fun TestClientRestControllerHandlerContext.pathParameterSerialization(parameter: RequestParameter) =
        parameter.content.matches(ContentType.ApplicationJson) {
            val model = parameter.content.model
            registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression(parameter.name.identifier(), model, ContentType.ApplicationJson)
            }.invoke("asString", "dependencyVogel".identifier().property("objectMapper"))
        }

    // don't adjust the model to the default value, to keep nullability even if there is a default value available
    private fun ContentInfo.forUserProvidedValue() = TypeInfo(model.asTypeReference(), model.getDefinedDefaultValue())

}