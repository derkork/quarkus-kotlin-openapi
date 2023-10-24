package com.ancientlightstudios.quarkus.kotlin.openapi

import org.jboss.resteasy.reactive.RestResponse


@Suppress("unused")
sealed interface RequestResult<T> {

    class Response<T>(val response: T) : RequestResult<T>
    class RequestError<T>(val reason: RequestErrorReason) : RequestResult<T>
    class ResponseError<T>(val reason: String, val response: RestResponse<*>) : RequestResult<T>

}

@Suppress("unused")
enum class RequestErrorReason {
    Timeout,
    Unreachable,
    ConnectionReset,
    Unknown
}
