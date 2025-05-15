package com.ancientlightstudios.quarkus.kotlin.openapi.handler

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ContentInfo

interface Handler {

    fun initializeContext(registry: HandlerRegistry) {
        // does nothing by default
    }

}
inline fun <R> ContentInfo.matches(contentType: ContentType, block: () -> R): HandlerResult<R> =
    this.contentType.matches(contentType, block)

inline fun <R> ContentType.matches(contentType: ContentType, block: () -> R): HandlerResult<R> {
    if (this != contentType) {
        return HandlerResult.Unhandled()
    }

    return HandlerResult.Handled(block())
}

inline fun <R> Feature.matches(feature: Feature, block: () -> R): HandlerResult<R> {
    if (this != feature) {
        return HandlerResult.Unhandled()
    }

    return HandlerResult.Handled(block())
}