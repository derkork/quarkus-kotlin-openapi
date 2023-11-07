package com.ancientlightstudios.quarkus.kotlin.openapi

@Suppress("unused")
enum class RequestErrorReason(val message: String) {
    Timeout("A timeout occurred when communicating with the server."),
    Unreachable("The server could not be reached."),
    ConnectionReset("The connection was reset while communicating with the server."),
    Unknown("An unknown error occurred when communicating with the server.")
}
