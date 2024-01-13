package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableResponse(
    var responseCode: ResponseCode,
    var body: TransformableBody? = null,
    var headers: List<TransformableHeaderParameter> = listOf()
): TransformableObject()