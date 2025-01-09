package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DefaultValue
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerBody
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.adjustToDefault
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class OctetServerRequestContainerHandler : ServerRequestContainerHandler {

    override val supportedContentType = ContentType.ApplicationOctetStream

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