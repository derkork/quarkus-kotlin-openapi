package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.SchemaProperty

data class ObjectProperty(val name: String, val source: SchemaProperty, val type: TypeDefinition)
