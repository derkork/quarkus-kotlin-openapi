package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.SerializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Misc
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.StaticContextExpression.Companion.staticContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.nullFallback
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class JsonSerializationHandler : SerializationHandler {

    override val supportedContentType = ContentType.ApplicationJson

    override fun serializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
        var result = source
        if (model.isNullable()) {
            result = result.nullCheck()
        }

        return when (val instance = model.instance) {
            is CollectionModelInstance -> nestedItemsSerialization(result, instance.items)
            is MapModelInstance -> nestedItemsSerialization(result, instance.items)
            is EnumModelInstance,
            is PrimitiveTypeModelInstance,
            is ObjectModelInstance,
            is OneOfModelInstance -> simpleSerialization(result)
        }
    }

    // produces:
    // <source>.asJson {
    //     <SerializationStatement for nested type> [?: NullNode.instance]
    // }
    private fun nestedItemsSerialization(source: KotlinExpression, itemModel: ModelInstance) =
        source.invoke("asJson") {
            val innerStatement = serializationExpression("it".identifier(), ModelUsage(itemModel))
            when (itemModel.isNullable()) {
                true -> innerStatement.nullFallback(Misc.NullNode.staticContext().property("instance"))
                else -> innerStatement
            }.statement()
        }

    // produces:
    // <source>.asJson()
    private fun simpleSerialization(source: KotlinExpression) = source.invoke("asJson")

}