package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableRequestBundle(
    var tag : String? = null,
    var requests: List<TransformableRequest> = listOf()
) : TransformableObject()