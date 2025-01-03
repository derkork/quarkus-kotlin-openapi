package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies whether a schema is a real model or just an overlay
object SchemaModeHint : Hint<SchemaMode> {

    var OpenApiSchema.schemaMode: SchemaMode
        get() = get(SchemaModeHint) ?: ProbableBug("Schema mode not set")
        set(value) = set(SchemaModeHint, value)

    fun OpenApiSchema.hasSchemaMode() = has(SchemaModeHint)

    fun OpenApiSchema.hasSchemaMode(schemaMode: SchemaMode) = get(SchemaModeHint) == schemaMode

}

enum class SchemaMode {

    // this schema is a real model. the type is specified by the schema target model hint
    Model,

    // this schema is just an overlay of another (model) schema, specified by the overlay target hint
    Overlay

}