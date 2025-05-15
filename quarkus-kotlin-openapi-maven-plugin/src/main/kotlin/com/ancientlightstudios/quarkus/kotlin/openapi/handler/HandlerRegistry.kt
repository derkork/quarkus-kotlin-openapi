package com.ancientlightstudios.quarkus.kotlin.openapi.handler

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class HandlerRegistry(val handlers: List<Handler>) {

    init {
        handlers.forEach { it.initializeContext(this) }
    }

    inline fun <reified H : Handler, R> getHandler(block: H.() -> HandlerResult<R>): R {
        val results = handlers.filterIsInstance<H>()
            .map(block)
            .filterIsInstance<HandlerResult.Handled<R>>()

        return when {
            results.isEmpty() -> ProbableBug("No handler found for interface ${H::class.simpleName}")
            results.size > 1 -> ProbableBug("Multiple active handler found for interface ${H::class.simpleName}")
            else -> results.first().result
        }
    }

}