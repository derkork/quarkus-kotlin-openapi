package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware

class OpenApiParameter(
    var name: String,
    var kind: ParameterKind,
    var required: Boolean,
    var content: OpenApiContentMapping
) : HintsAware()
