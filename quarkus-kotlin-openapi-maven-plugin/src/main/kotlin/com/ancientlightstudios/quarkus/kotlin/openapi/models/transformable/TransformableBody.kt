package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableBody(
    var required: Boolean,
    var contentTypes: List<ContentType> = listOf()
) : TransformableObject()