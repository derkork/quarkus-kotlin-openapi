package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware

class OpenApiSpec(
    var bundles: List<OpenApiRequestBundle> = listOf(),
    var schemas: List<OpenApiSchema> = listOf(),
    var version: String? = null
) : HintsAware()