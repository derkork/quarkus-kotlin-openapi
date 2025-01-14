package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DefaultValue
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerBody
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.adjustToDefault
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class JsonServerRequestContainerHandler : ServerRequestContainerHandler {

    override fun KotlinClass.emitRequestContainerParameter(
        parameter: ServerRequestContainerParameter, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationJson) {
        val model = parameter.content.model
        val defaultValue = model.instance.getDefaultValue()
        emitDefaultRequestContainerParameter(parameter.name, model.adjustToDefault(defaultValue))
    }

    override fun KotlinClass.emitRequestContainerBody(
        body: ServerRequestContainerBody, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationJson) {
        val model = body.content.model
        val defaultValue = model.instance.getDefaultValue()
        emitDefaultRequestContainerBody(body.name, model.adjustToDefault(defaultValue))
    }

    // default value for a response value can be different to the default value of a request value
    private fun ModelInstance.getDefaultValue(): DefaultValue = when (this) {
        is CollectionModelInstance -> DefaultValue.None
        is EnumModelInstance -> when (defaultValue) {
            null -> DefaultValue.None
            else -> DefaultValue.EnumValue(ref, defaultValue)
        }

        is MapModelInstance -> DefaultValue.None
        is ObjectModelInstance -> DefaultValue.None
        is OneOfModelInstance -> DefaultValue.None
        is PrimitiveTypeModelInstance -> when (defaultValue) {
            null -> DefaultValue.None
            else -> DefaultValue.StaticValue(itemType, defaultValue)
        }
    }
}

