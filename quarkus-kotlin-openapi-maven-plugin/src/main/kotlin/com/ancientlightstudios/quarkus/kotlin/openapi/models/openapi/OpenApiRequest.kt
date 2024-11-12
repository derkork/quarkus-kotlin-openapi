package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware

class OpenApiRequest(
    var path: String,
    var method: RequestMethod,
    var operationId: String? = null,
    var tags: List<String> = listOf(),
    var parameters: List<OpenApiParameter> = listOf(),
    var body: OpenApiBody? = null,
    var responses: List<OpenApiResponse> = listOf()
) : HintsAware() {

    fun hasInputParameter() = parameters.isNotEmpty() || body != null

}