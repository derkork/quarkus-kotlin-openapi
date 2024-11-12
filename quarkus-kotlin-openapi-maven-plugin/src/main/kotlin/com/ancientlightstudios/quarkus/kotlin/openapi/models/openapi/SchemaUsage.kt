package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

interface SchemaUsage {

    var schema: OpenApiSchema

}

class DefaultSchemaUsage(override var schema: OpenApiSchema) : SchemaUsage