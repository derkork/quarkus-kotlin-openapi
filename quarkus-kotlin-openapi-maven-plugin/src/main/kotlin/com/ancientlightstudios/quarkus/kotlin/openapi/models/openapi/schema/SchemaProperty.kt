package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class SchemaProperty(
    val schema: Schema,
    val direction: Direction,
    val description: String?,
    val required: Boolean
)
