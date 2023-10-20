package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

enum class Direction(private val value: String) {
    // a property marked as readOnly is only available in the response
    ReadOnly("readOnly"),

    // a property marked as writeOnly is only available in the request
    WriteOnly("writeOnly"),
    ReadAndWrite("readAndWrite")
}