package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware

class OpenApiSchemaProperty(
    var name: String,
    override var schema: OpenApiSchema
) : HintsAware(), SchemaUsage
