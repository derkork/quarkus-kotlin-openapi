package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableSpec(
    var bundles: List<TransformableRequestBundle> = listOf(),
    var schemaDefinitions: List<TransformableSchemaDefinition> = listOf(),
    var version: String? = null
) : TransformableObject()