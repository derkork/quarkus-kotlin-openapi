package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware

class OpenApiRequestBundle(
    var tag : String? = null,
    var requests: List<OpenApiRequest> = listOf()
) : HintsAware()