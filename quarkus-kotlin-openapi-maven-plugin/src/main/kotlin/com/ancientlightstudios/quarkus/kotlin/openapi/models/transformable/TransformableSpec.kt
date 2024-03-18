package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableSpec(
    var bundles: List<TransformableRequestBundle> = listOf(),
    var schemas: List<TransformableSchema> = listOf(),
    var version: String? = null
) : TransformableObject()