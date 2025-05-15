package com.ancientlightstudios.quarkus.kotlin.openapi.handler

sealed interface HandlerResult<R> {

    class Unhandled<R> : HandlerResult<R>
    data class Handled<R>(val result: R) : HandlerResult<R>

}