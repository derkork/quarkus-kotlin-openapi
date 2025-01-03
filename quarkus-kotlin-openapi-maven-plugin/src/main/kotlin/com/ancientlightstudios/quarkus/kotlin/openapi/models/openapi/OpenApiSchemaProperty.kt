package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaContainer

class OpenApiSchemaProperty(var name: String, override var schema: OpenApiSchema) : HintsAware(), SchemaContainer
