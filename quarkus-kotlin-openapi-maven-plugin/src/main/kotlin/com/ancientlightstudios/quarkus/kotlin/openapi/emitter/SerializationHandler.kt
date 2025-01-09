package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.ContentTypeHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage

interface SerializationHandler : ContentTypeHandler {

    fun serializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression

}