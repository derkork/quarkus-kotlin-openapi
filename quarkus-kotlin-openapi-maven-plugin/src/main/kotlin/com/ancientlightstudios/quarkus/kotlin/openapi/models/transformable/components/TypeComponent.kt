package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes

class TypeComponent(var types: List<SchemaTypes> = listOf(), var format: String? = null) : SchemaDefinitionComponent