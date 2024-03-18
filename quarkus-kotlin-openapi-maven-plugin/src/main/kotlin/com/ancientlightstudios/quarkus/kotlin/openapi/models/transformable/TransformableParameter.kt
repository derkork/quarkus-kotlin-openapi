package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableParameter(
    var name: String,
    var kind: ParameterKind,
    var required: Boolean,
    override var schema: TransformableSchema
) : TransformableObject(), SchemaUsage
