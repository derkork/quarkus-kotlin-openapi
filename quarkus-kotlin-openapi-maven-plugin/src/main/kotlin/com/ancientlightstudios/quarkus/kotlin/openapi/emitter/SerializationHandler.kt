package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage

/**
 * handler of this type can produce an expression to serialize a value based on the given model
 */
interface SerializationHandler : Handler {

    fun serializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ): HandlerResult<KotlinExpression>

}