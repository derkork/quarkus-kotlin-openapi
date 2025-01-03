package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

sealed interface ModelClass {

    val direction: SchemaDirection

    val source: OpenApiSchema

}