package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

interface SchemaUsage {

    var schema: TransformableSchema

}

class DefaultSchemaUsage(override var schema: TransformableSchema) : SchemaUsage