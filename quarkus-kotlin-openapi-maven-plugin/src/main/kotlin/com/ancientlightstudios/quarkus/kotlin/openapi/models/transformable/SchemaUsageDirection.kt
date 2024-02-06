package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

enum class SchemaUsageDirection {
    // a schema marked as readOnly is only available in the response
    ReadOnly,

    // a schema marked as writeOnly is only available in the request
    WriteOnly
}