package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

enum class FlowDirection {
    // type is only used to send data from client to server
    Up,

    // type is only used to send data from server to client
    Down
}