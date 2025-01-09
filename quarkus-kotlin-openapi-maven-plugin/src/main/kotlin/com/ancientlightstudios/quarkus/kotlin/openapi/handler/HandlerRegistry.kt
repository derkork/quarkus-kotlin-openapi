package com.ancientlightstudios.quarkus.kotlin.openapi.handler

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class HandlerRegistry(val handlers: List<Handler>) {

    init {
        handlers.forEach { it.initializeContext(this) }
    }

    inline fun <reified T : ContentTypeHandler> getHandler(contentType: ContentType): T {
        val result = handlers.filterIsInstance<T>().filter { it.supportedContentType == contentType }
        return when {
            result.isEmpty() -> ProbableBug("No handler found for interface ${T::class.simpleName} and content type ${contentType.value}")
            result.size > 1 -> ProbableBug("Multiple handler found for interface ${T::class.simpleName} and content type ${contentType.value}")
            else -> result.first()
        }
    }

    inline fun <reified T : FeatureHandler> getHandler(feature: Feature): T {
        val result = handlers.filterIsInstance<T>().filter { it.canHandleFeature(feature) }
        return when {
            result.isEmpty() -> ProbableBug("No handler found for interface ${T::class.simpleName} and feature ${feature::class.simpleName}")
            result.size > 1 -> ProbableBug("Multiple handler found for interface ${T::class.simpleName} and feature ${feature::class.simpleName}")
            else -> result.first()
        }
    }


}