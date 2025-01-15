package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DeserializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.SerializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.withValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.nullFallback
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.wrap
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.ModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationMode
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class OctetModelTransformationHandler : ModelTransformationHandler, DeserializationHandler, SerializationHandler {

    override fun registerTransformations(
        model: ModelClass, mode: TransformationMode, contentType: ContentType
        // we still have to check if we are allowed/required to act
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        // nothing to do
    }

    override fun deserializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        var result = when (val instance = model.instance) {
            is CollectionModelInstance -> ProbableBug("Octet deserialization not supported for collections")
            is EnumModelInstance -> ProbableBug("Octet deserialization not supported for enums")
            is MapModelInstance -> ProbableBug("Octet deserialization not supported for maps")
            is ObjectModelInstance -> ProbableBug("Octet deserialization not supported for objects")
            is OneOfModelInstance -> ProbableBug("Octet deserialization not supported for oneOfs")
            is PrimitiveTypeModelInstance -> primitiveDeserialization(source, instance)
        }

        if (!model.isNullable()) {
            result = result.wrap().invoke("required")
        }
        result
    }

    override fun serializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        when (model.instance) {
            is CollectionModelInstance -> ProbableBug("Octet serialization not supported for collections")
            is EnumModelInstance -> ProbableBug("Octet serialization not supported for enums")
            is MapModelInstance -> ProbableBug("Octet serialization not supported for maps")
            is ObjectModelInstance -> ProbableBug("Octet serialization not supported for objects")
            is OneOfModelInstance -> ProbableBug("Octet serialization not supported for oneOfs")
            is PrimitiveTypeModelInstance -> when (model.isNullable()) {
                // null and empty arrays are the same for octet, so we work with non-nullable below the surface
                true -> source.nullFallback(invoke("byteArrayOf"))
                false -> source
            }
        }
    }

    // produces:
    // <source>
    //     [ValidationStatements]
    private fun primitiveDeserialization(
        source: KotlinExpression, model: PrimitiveTypeModelInstance
    ): KotlinExpression {
        return withValidation(source, model)
    }

}