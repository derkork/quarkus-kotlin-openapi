package com.ancientlightstudios.quarkus.kotlin.openapi



@Suppress("unused")
sealed interface RequestResult<T> {

    class Response<T>(val response: T) : RequestResult<T>
    class RequestError<T>(val reason: RequestErrorReason) : RequestResult<T>
    class ResponseError<T>(val reason: String, val response: jakarta.ws.rs.core.Response) : RequestResult<T>

}

@Suppress("unused")
enum class RequestErrorReason {
    Timeout,
    Unreachable,
    ConnectionReset,
    Unknown
}
