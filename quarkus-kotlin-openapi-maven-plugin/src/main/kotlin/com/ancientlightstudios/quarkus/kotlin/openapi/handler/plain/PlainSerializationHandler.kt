package com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.SerializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class PlainSerializationHandler : SerializationHandler {

    override val supportedContentType = ContentType.TextPlain

    override fun serializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
        var result = source
        if (model.isNullable()) {
            result = result.nullCheck()
        }

        return when (val instance = model.instance) {
            is CollectionModelInstance -> collectionSerialization(result, instance)
            is EnumModelInstance,
            is PrimitiveTypeModelInstance -> primitiveSerialization(result)

            is MapModelInstance -> ProbableBug("Maps not supported for content type ${supportedContentType.value}")
            is ObjectModelInstance,
            is OneOfModelInstance -> ProbableBug("Objects not supported for content type ${supportedContentType.value}")
        }
    }

    // produces:
    // <source>.map {
    //     <SerializationStatement for nested type>
    // }
    private fun collectionSerialization(source: KotlinExpression, model: CollectionModelInstance) =
        source.invoke("map") {
            serializationExpression("it".identifier(), ModelUsage(model.items)).statement()
        }

    // produces:
    // <source>.asString()
    private fun primitiveSerialization(source: KotlinExpression) = source.invoke("asString")

}