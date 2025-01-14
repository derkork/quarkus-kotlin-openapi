package com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EnumModelSerializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ModelSerializationFeature
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.SerializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class PlainSerializationHandler : SerializationHandler, EnumModelSerializationHandler {

    override fun serializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.TextPlain) {
        serializationExpression(source, model)
    }

    private fun serializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
        var result = source
        if (model.isNullable()) {
            result = result.nullCheck()
        }

        return when (val instance = model.instance) {
            is CollectionModelInstance -> collectionSerialization(result, instance)
            is EnumModelInstance,
            is PrimitiveTypeModelInstance -> primitiveSerialization(result)

            is MapModelInstance -> ProbableBug("Plain serialization not supported for maps")
            is ObjectModelInstance -> ProbableBug("Plain serialization not supported for objects")
            is OneOfModelInstance -> ProbableBug("Plain serialization not supported for oneOfs")
        }
    }

    // produces:
    // <source>.map {
    //     <SerializationStatement for nested type>
    // }
    private fun collectionSerialization(source: KotlinExpression, model: CollectionModelInstance) =
        source.invoke("map") {
            serializationExpression("it".identifier(), model.items).statement()
        }

    // produces:
    // <source>.asString()
    private fun primitiveSerialization(source: KotlinExpression) = source.invoke("asString")

    // produces
    // fun asString(): String = value.asString()
    override fun KotlinEnum.installSerializationFeature(model: EnumModelClass, feature: ModelSerializationFeature) =
        feature.matches(PlainSerializationFeature) {
            kotlinMethod("asString", returnType = Kotlin.String.asTypeReference(), bodyAsAssignment = true) {
                "value".identifier().invoke("asString").statement()
            }
        }

}