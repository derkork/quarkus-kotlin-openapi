package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware

class OpenApiBody(var required: Boolean, var content: OpenApiContentMapping) : HintsAware()