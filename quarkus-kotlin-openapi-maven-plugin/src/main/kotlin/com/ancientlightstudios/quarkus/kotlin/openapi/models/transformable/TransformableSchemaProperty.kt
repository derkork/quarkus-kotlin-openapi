package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableSchemaProperty(
    var name: String,
    override var schema: TransformableSchema
) : TransformableObject(), SchemaUsage
