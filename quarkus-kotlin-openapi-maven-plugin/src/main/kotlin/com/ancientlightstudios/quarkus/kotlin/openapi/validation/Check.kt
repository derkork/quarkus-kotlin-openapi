package com.ancientlightstudios.quarkus.kotlin.openapi.validation

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

interface Check {

    fun verify(spec: OpenApiSpec)

}