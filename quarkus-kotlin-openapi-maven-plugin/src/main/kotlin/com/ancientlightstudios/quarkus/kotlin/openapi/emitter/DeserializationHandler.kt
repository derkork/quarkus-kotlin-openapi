package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage

/**
 * handler of this type can produce an expression to deserialize a value based on the given model
 */
interface DeserializationHandler : Handler {

    fun deserializationExpression(source: KotlinExpression, model: ModelUsage, contentType: ContentType):
            HandlerResult<KotlinExpression>

}