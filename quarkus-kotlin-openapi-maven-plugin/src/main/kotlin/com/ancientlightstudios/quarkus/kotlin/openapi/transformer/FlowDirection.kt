package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

// the value is used as a bitmask. Only use power of two numbers
enum class FlowDirection(val value: Byte) {
    // type is only used to send data from client to server
    Up(1),

    // type is only used to send data from server to client
    Down(2)
}