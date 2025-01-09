package com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DefaultValue
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerBody
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.adjustToDefault
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class PlainServerRequestContainerHandler : ServerRequestContainerHandler {

    override val supportedContentType = ContentType.TextPlain

    override fun KotlinClass.emitRequestContainerParameter(parameter: ServerRequestContainerParameter) {
        val model = parameter.content.model
        val defaultValue = model.instance.getDefaultValue()
        emitDefaultRequestContainerParameter(parameter.name, model.adjustToDefault(defaultValue))
    }

    override fun KotlinClass.emitRequestContainerBody(body: ServerRequestContainerBody) {
        val model = body.content.model
        val defaultValue = model.instance.getDefaultValue()
        emitDefaultRequestContainerBody(body.name, model.adjustToDefault(defaultValue))
    }

    // default value for a request value can be different to the default value of a response value
    private fun ModelInstance.getDefaultValue(): DefaultValue = when (this) {
        is CollectionModelInstance -> DefaultValue.EmptyList
        is EnumModelInstance -> when (defaultValue) {
            null -> DefaultValue.None
            else -> DefaultValue.EnumValue(ref, defaultValue)
        }

        is MapModelInstance -> DefaultValue.EmptyMap
        is ObjectModelInstance,
        is OneOfModelInstance -> DefaultValue.None // incompatible with plain
        is PrimitiveTypeModelInstance -> when (defaultValue) {
            null -> DefaultValue.None
            else -> DefaultValue.StaticValue(itemType, defaultValue)
        }
    }
}