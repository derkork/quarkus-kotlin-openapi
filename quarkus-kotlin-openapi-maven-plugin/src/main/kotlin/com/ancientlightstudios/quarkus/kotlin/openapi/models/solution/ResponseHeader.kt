package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiParameter

class ResponseHeader(
    val name: String,
    val content: ContentInfo,
    val source: OpenApiParameter,
    val context: String = "response.${source.kind.value}.${source.name}"
) {

    val sourceName = source.name
    val kind = source.kind

}