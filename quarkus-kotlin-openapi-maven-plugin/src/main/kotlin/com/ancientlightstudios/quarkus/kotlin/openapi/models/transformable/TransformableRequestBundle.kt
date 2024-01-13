package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableRequestBundle(
    var requests: List<TransformableRequest> = listOf()
) : TransformableObject()