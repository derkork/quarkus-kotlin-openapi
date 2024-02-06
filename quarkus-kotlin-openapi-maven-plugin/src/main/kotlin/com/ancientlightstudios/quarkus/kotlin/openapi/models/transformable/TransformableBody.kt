package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableBody(
    var required: Boolean,
    var content: List<ContentMapping> = listOf()
) : TransformableObject()