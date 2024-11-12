package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

interface GeneratorStage {

    fun process(spec: OpenApiSpec)

}