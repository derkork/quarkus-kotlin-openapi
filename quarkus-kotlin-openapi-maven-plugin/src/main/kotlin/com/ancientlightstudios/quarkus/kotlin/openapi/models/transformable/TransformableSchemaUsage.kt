package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

// marker for every place where a schema can be used. It's like a hull pointing to a schema definition.
class TransformableSchemaUsage(var schemaDefinition: TransformableSchemaDefinition) : TransformableObject()