package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DeserializationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.withValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.wrap
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class OctetDeserializationHandler : DeserializationHandler {

    override fun deserializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        deserializationExpression(source, model)
    }

    private fun deserializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
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
        return result
    }

    // produces:
    // <source>.emptyArrayAsNull()
    //     [ValidationStatements]
    private fun primitiveDeserialization(
        source: KotlinExpression, model: PrimitiveTypeModelInstance
    ): KotlinExpression {
        var result = source.invoke("emptyArrayAsNull")
        result = withValidation(result, model)
        return result
    }

}