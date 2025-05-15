package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware

class OpenApiResponse(
    var responseCode: ResponseCode,
    var body: OpenApiBody? = null,
    var headers: List<OpenApiParameter> = listOf(),
    var interfaceName: String? = null
) : HintsAware()