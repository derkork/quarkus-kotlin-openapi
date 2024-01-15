package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

class TransformableRequest(
    var path: String,
    var method: RequestMethod,
    var operationId: String?,
    var tags: List<String> = listOf(),
    var parameters: List<TransformableParameter> = listOf(),
    var body: TransformableBody? = null,
    var responses: List<TransformableResponse> = listOf()
) : TransformableObject()