package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContextEmitter.Companion.emitDefaultResponseMethodBody
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContextEmitter.Companion.emitDefaultResponseMethodHeader
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerResponseInterfaceEmitter.Companion.emitDefaultResponseInterfaceHeader
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class OctetServerResponseHandler : ServerResponseInterfaceHandler, ServerRequestContextHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun KotlinMethod.emitResponseInterfaceHeader(
        header: ServerResponseHeader, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        val model = header.content.model
        val defaultValue = model.instance.getDefaultValue()
        emitDefaultResponseInterfaceHeader(header.name, model.adjustToDefault(defaultValue), defaultValue)
    }

    override fun KotlinMethod.emitResponseInterfaceBody(
        body: ServerResponseBody, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        val model = body.content.model
        val defaultValue = model.instance.getDefaultValue()
        emitDefaultResponseInterfaceHeader(body.name, model.adjustToDefault(defaultValue), defaultValue)
    }

    override fun KotlinMethod.emitResponseMethodHeader(
        header: ServerResponseHeader, fromInterface: Boolean, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        val model = header.content.model
        val defaultValue = model.instance.getDefaultValue()
        val finalModel = model.adjustToDefault(defaultValue)
        emitDefaultResponseMethodHeader(header.name, finalModel, defaultValue, fromInterface)

        registry.getHandler<SerializationHandler, KotlinExpression> {
            serializationExpression(header.name.identifier(), finalModel, ContentType.ApplicationOctetStream)
        }
    }

    override fun KotlinMethod.emitResponseMethodBody(
        body: ServerResponseBody, fromInterface: Boolean, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        val model = body.content.model
        val defaultValue = model.instance.getDefaultValue()
        val finalModel = model.adjustToDefault(defaultValue)
        emitDefaultResponseMethodBody(body.name, finalModel, defaultValue, fromInterface)

        registry.getHandler<SerializationHandler, KotlinExpression> {
            serializationExpression(body.name.identifier(), finalModel, ContentType.ApplicationOctetStream)
        }
    }

    // default value for a response value can be different to the default value of a request value
    private fun ModelInstance.getDefaultValue(): DefaultValue = when (this) {
        is CollectionModelInstance,
        is EnumModelInstance,
        is MapModelInstance,
        is ObjectModelInstance,
        is OneOfModelInstance -> DefaultValue.None // incompatible with octet
        is PrimitiveTypeModelInstance -> when (itemType) {
            is BaseType.ByteArray -> DefaultValue.EmptyByteArray
            else -> DefaultValue.None // incompatible with octet
        }
    }
}