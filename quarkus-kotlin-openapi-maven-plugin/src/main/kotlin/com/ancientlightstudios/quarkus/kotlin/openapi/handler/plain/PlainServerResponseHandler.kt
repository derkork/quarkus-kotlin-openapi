package com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContextEmitter.Companion.emitDefaultResponseMethodBody
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContextEmitter.Companion.emitDefaultResponseMethodHeader
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerResponseInterfaceEmitter.Companion.emitDefaultResponseInterfaceHeader
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyVogelHandler

class PlainServerResponseHandler : ServerResponseInterfaceHandler, ServerRequestContextHandler, DependencyVogelHandler {

    private lateinit var serialization: SerializationHandler

    override val supportedContentType = ContentType.TextPlain

    override fun initializeContext(registry: HandlerRegistry) {
        serialization = registry.getHandler<SerializationHandler>(supportedContentType)
    }

    override fun KotlinMethod.emitResponseInterfaceHeader(header: ServerResponseHeader) {
        val model = header.content.model
        val defaultValue = model.instance.getDefaultValue()
        emitDefaultResponseInterfaceHeader(header.name, model.adjustToDefault(defaultValue), defaultValue)
    }

    override fun KotlinMethod.emitResponseInterfaceBody(body: ServerResponseBody) {
        val model = body.content.model
        val defaultValue = model.instance.getDefaultValue()
        emitDefaultResponseInterfaceHeader(body.name, model.adjustToDefault(defaultValue), defaultValue)
    }

    override fun KotlinMethod.emitResponseMethodHeader(
        header: ServerResponseHeader, fromInterface: Boolean
    ): KotlinExpression {
        val model = header.content.model
        val defaultValue = model.instance.getDefaultValue()
        val finalModel = model.adjustToDefault(defaultValue)
        emitDefaultResponseMethodHeader(header.name, finalModel, defaultValue, fromInterface)

        return serialization.serializationExpression(header.name.identifier(), finalModel)
    }

    override fun KotlinMethod.emitResponseMethodBody(
        body: ServerResponseBody, fromInterface: Boolean
    ): KotlinExpression {
        val model = body.content.model
        val defaultValue = model.instance.getDefaultValue()
        val finalModel = model.adjustToDefault(defaultValue)
        emitDefaultResponseMethodBody(body.name, finalModel, defaultValue, fromInterface)

        return serialization.serializationExpression(body.name.identifier(), finalModel)
    }

    override fun installFeatureFor(dependencyVogel: DependencyVogel) {
        // nothing to do
    }

    // default value for a response value can be different to the default value of a request value
    private fun ModelInstance.getDefaultValue(): DefaultValue = when (this) {
        is CollectionModelInstance -> DefaultValue.EmptyList
        is EnumModelInstance -> when (defaultValue) {
            null -> DefaultValue.nullOrNone(isNullable())
            else -> DefaultValue.EnumValue(ref, defaultValue)
        }

        is MapModelInstance -> DefaultValue.EmptyMap
        is ObjectModelInstance,
        is OneOfModelInstance -> DefaultValue.None // incompatible with plain
        is PrimitiveTypeModelInstance -> when (defaultValue) {
            null -> DefaultValue.nullOrNone(isNullable())
            else -> DefaultValue.StaticValue(itemType, defaultValue)
        }
    }
}

