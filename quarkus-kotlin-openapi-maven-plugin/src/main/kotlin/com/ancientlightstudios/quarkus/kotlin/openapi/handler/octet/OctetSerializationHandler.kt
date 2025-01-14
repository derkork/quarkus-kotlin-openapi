package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.SerializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class OctetSerializationHandler : SerializationHandler {

    override fun serializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        when (model.instance) {
            is CollectionModelInstance -> ProbableBug("Octet serialization not supported for collections")
            is EnumModelInstance -> ProbableBug("Octet serialization not supported for enums")
            is MapModelInstance -> ProbableBug("Octet serialization not supported for maps")
            is ObjectModelInstance -> ProbableBug("Octet serialization not supported for objects")
            is OneOfModelInstance -> ProbableBug("Octet serialization not supported for oneOfs")
            // just use the source expression without any null-check as the result
            is PrimitiveTypeModelInstance -> source
        }
    }

}