package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.SerializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class OctetSerializationHandler : SerializationHandler {

    override val supportedContentType = ContentType.ApplicationOctetStream

    override fun serializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
        return when (model.instance) {
            is CollectionModelInstance -> ProbableBug("Collections not supported for content type ${supportedContentType.value}")
            is EnumModelInstance -> ProbableBug("Enums not supported for content type ${supportedContentType.value}")
            is MapModelInstance -> ProbableBug("Maps not supported for content type ${supportedContentType.value}")
            is ObjectModelInstance,
            is OneOfModelInstance -> ProbableBug("Objects not supported for content type ${supportedContentType.value}")

            // just use the source expression without any null-check as the result
            is PrimitiveTypeModelInstance -> source
        }
    }

}